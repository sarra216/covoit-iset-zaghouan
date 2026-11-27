# DevOps mini-project applied to Covoit ISET Zaghouan · step by step

This guide follows your 5 PDFs (steps 1, 2, 3, 5, 6) plus step 4, applied to the **Covoit ISET** project instead of the small Flask demo app.

| Guide (Flask demo) | Covoit ISET version |
|---|---|
| `app.py` with `/health` → `{"status":"ok"}` | `HealthController.java`: `GET /health` → `{"status":"ok","version":"<commit>","base":"ok"}` |
| `requirements.txt` + `pip install --user` | `pom.xml` + Maven in a **build stage**; only the `.jar` goes into the final image |
| Postgres | MySQL 8.4 (service `base`) |
| App on port 3000 | Covoit published on port **3000** (8080 inside the container) |
| GitLab on port 8080 | GitLab on port **8929**: port 8080 is already used on your PC by Oracle (`TNSLSNR.EXE`) |
| Harbor (port 80) | Harbor on port **8085**, or the simpler `registry:2` on port 5000 |
| `pyinfra @docker/ubuntu deploy.py` | `pyinfra @local deploy.py`: it deploys on your own machine, where Docker runs |

## 0. Prepare Windows (once)

The guides use Linux commands. On Windows, run them in **WSL 2 (Ubuntu)** with **Docker Desktop**:

1. In PowerShell **as administrator**, run `wsl --install -d Ubuntu`, then restart the PC. Open "Ubuntu" from the Start menu and choose a username and password.
2. Install **Docker Desktop**. In Settings › Resources › **WSL integration**, turn on **Ubuntu**.
3. GitLab needs memory. Create `C:\Users\khadi\.wslconfig` with:
   ```ini
   [wsl2]
   memory=6GB
   ```
   Then run `wsl --shutdown` in PowerShell.
4. Turn on systemd so the Runner can run as a service. In Ubuntu:
   ```bash
   echo -e "[boot]\nsystemd=true" | sudo tee /etc/wsl.conf
   ```
   Then run `wsl --shutdown` in PowerShell and reopen Ubuntu.
5. Check in Ubuntu: `docker ps` must work without error.

Your project folder is reachable from Ubuntu at `/mnt/c/Users/khadi/covoit-iset-zaghouan`. You can keep using Git Bash for git, and use Ubuntu for the Runner, pyinfra and Harbor.

---

## Step 1: Git and the workflow (GitLab CE)

**1.1 Start GitLab.** In Ubuntu:

```bash
mkdir -p ~/gitlab/{config,logs,data}

docker run -d \
  --hostname localhost \
  --name gitlab \
  --restart unless-stopped \
  -p 8929:8929 -p 2222:22 \
  -e GITLAB_OMNIBUS_CONFIG="external_url 'http://localhost:8929'; gitlab_rails['gitlab_shell_ssh_port'] = 2222" \
  -v ~/gitlab/config:/etc/gitlab \
  -v ~/gitlab/logs:/var/log/gitlab \
  -v ~/gitlab/data:/var/opt/gitlab \
  --shm-size 256m \
  gitlab/gitlab-ce:latest
```

- The first start takes **3 to 5 minutes**. Follow it with `docker logs -f gitlab`.
- Open **http://localhost:8929**. The user is `root`. Recent versions generate the password; read it with:
  ```bash
  docker exec -it gitlab grep 'Password:' /etc/gitlab/initial_root_password
  ```
  Then change it in your profile, because that file is deleted after 24 h.

**1.2 Create the project and push the code.**
1. In GitLab: **New project › Create blank project**. Name it `covoit-iset-zaghouan` and **untick** "Initialize repository with a README".
2. In Git Bash, in your project:
   ```bash
   cd ~/covoit-iset-zaghouan
   git remote add gitlab http://localhost:8929/root/covoit-iset-zaghouan.git
   git push gitlab main
   ```
   The code is now on **both GitHub (`origin`) and GitLab (`gitlab`)**.

**1.3 Branching strategy: trunk-based.** `main` is always deployable. Each feature lives on a short branch, `feature/xxx`, and reaches `main` only through a Merge Request.

**1.4 Protect `main`.** Go to Settings › Repository › **Protected branches** › `main`:
- Allowed to push and merge → **No one**
- Allowed to merge → **Maintainers**

**1.5 First Merge Request.** This is the step 1 deliverable. Add the health endpoint from this delivery on a branch:

```bash
git checkout -b feature/add-health-endpoint
# copy HealthController.java, HealthControllerTest.java, Dockerfile, docker-compose.yml from the zip
git add .
git commit -m "feat: add health endpoint"
git push gitlab feature/add-health-endpoint
```

In GitLab, open the **Merge Request** suggested at the top of the page and merge it.

✅ **Deliverables:** local GitLab working · `main` protected · `feature/add-health-endpoint` merged through a Merge Request.

---

## Step 2: Containerise the application

Files: `Dockerfile` (multi-stage), `.dockerignore`, `docker-compose.yml`, `.env.exemple`.

```bash
cp .env.exemple .env          # fill in DATABASE_PASSWORD, MYSQL_ROOT_PASSWORD, JWT_SECRET
docker compose up -d --build
curl http://localhost:3000/health
# → {"status":"ok","version":"dev","base":"ok"}
docker compose down
```

**What to explain to the teacher:**
- **Multi-stage build.** Stage 1 (`maven:3.9-eclipse-temurin-17`) compiles the `.jar`. Stage 2 (`eclipse-temurin:17-jre`) contains only Java and the `.jar`: no Maven, no source code. This is the Java equivalent of `pip install --user` plus copying only `.local`. The final image is smaller, with less attack surface.
- **Non-root user** (`useradd covoit` + `USER covoit`). If someone exploits a flaw in the app, they only get the rights of a limited user, not root inside the container.
- **`base` = DNS name.** The app connects to `jdbc:mysql://base:3306/covoit` inside the `appnet` network.
- **`healthcheck` + `condition: service_healthy`.** The app waits until MySQL actually answers.
- **Named volume `donnees-mysql`.** The data survives `docker compose down`.
- **`.env` is never committed** (it's in `.gitignore`). Only `.env.exemple`, which has no secrets, is in Git.

✅ **Deliverables:** the multi-stage Dockerfile builds · `docker compose up` starts both services · `/health` answers.

---

## Step 3: GitLab CI/CD pipeline

**3.1 Install the Runner** (in Ubuntu, the guide's commands as they are):

```bash
sudo curl -L --output /usr/local/bin/gitlab-runner \
  https://gitlab-runner-downloads.s3.amazonaws.com/latest/binaries/gitlab-runner-linux-amd64
sudo chmod +x /usr/local/bin/gitlab-runner
sudo useradd --comment 'GitLab Runner' --create-home gitlab-runner --shell /bin/bash
sudo gitlab-runner install --user=gitlab-runner --working-directory=/home/gitlab-runner
sudo gitlab-runner start

# The "test" job needs Java 17 and Maven; the "build" job needs Docker
sudo apt update && sudo apt install -y openjdk-17-jdk maven
sudo usermod -aG docker gitlab-runner      # common pitfall #2
sudo gitlab-runner restart
```

**3.2 Register it.**
1. In GitLab, go to Project › Settings › CI/CD › **Runners** › **New project runner**.
2. Choose Linux, add the tag **`local`**, and copy the token (it starts with `glrt-`).
3. Register:
   ```bash
   sudo gitlab-runner register \
     --url http://localhost:8929/ \
     --token <YOUR_TOKEN> \
     --executor shell \
     --description "local-shell-runner"
   ```
4. Check with `sudo gitlab-runner list`. The runner shows a green dot in GitLab.

**3.3 The pipeline (`.gitlab-ci.yml`).**

| Job | When | What it does |
|---|---|---|
| `test` | Every push, every branch (so every Merge Request) | `mvn -B test`: the unit tests. GitLab shows each test's result in the MR. |
| `build-image` | `main` only | `docker build --build-arg VERSION=$CI_COMMIT_SHORT_SHA -t covoit-iset:<commit> .` plus the `latest` tag |
| `push-image` | `main`, if `REGISTRY` is set | Pushes the image to Harbor (step 4) |

**3.4 Trigger it.** `main` is protected, so go through a branch:
```bash
git checkout main && git pull gitlab main
git checkout -b ci/add-pipeline
git add .gitlab-ci.yml
git commit -m "ci: add GitLab CI pipeline"
git push gitlab ci/add-pipeline
```
- On the MR, the **test** job runs. Merge the MR, and **test → build** run on `main`.
- To see it: **Build › Pipelines**. Then in Ubuntu, `docker images | grep covoit-iset` shows the image tagged with the commit.
- If the jobs stay **pending**, the tag doesn't match. The runner must have the tag `local`.

✅ **Deliverables:** Runner registered · pipeline on every push · `test` passes · `build` produces an image.

---

## Step 4: Private registry (Harbor) and automated deployment (pyinfra)

The step 4 PDF wasn't among your files. This step is rebuilt from what steps 5 and 6 expect: images pushed to **Harbor** by the CI, and **`deploy.py`** with pyinfra.

**4.1 Harbor** (in Ubuntu):
```bash
cd ~
# Take the latest "harbor-offline-installer-vX.Y.Z.tgz" from https://github.com/goharbor/harbor/releases
wget https://github.com/goharbor/harbor/releases/download/v2.11.1/harbor-offline-installer-v2.11.1.tgz
tar xzvf harbor-offline-installer-v2.11.1.tgz
cd harbor
cp harbor.yml.tmpl harbor.yml
nano harbor.yml
```

In `harbor.yml`:
- `hostname: localhost`
- `http:` → `port: 8085`
- **comment out the whole `https:` block** (common pitfall #3: otherwise you get TLS errors)
- `harbor_admin_password:` → choose one

Then:
```bash
sudo ./install.sh
```

1. Open http://localhost:8085 and log in with `admin` and your password.
2. Create a **project** named `covoit`.
3. In GitLab › Settings › CI/CD › **Variables**, add:
   - `REGISTRY` = `localhost:8085/covoit`
   - `REGISTRY_USER` = `admin` (or a Harbor robot account)
   - `REGISTRY_PASSWORD` = the password, **Masked** (common pitfall #7: never put it in `.gitlab-ci.yml`)

> **Lighter alternative** (pitfall #5 of the guide, if you're short on time or RAM):
> `docker run -d -p 5000:5000 --name registre --restart unless-stopped registry:2`
> Then set only `REGISTRY` = `localhost:5000`, with no user or password.

Docker allows plain-HTTP registries on `localhost`, so no extra configuration is needed.

**4.2 pyinfra.**
```bash
sudo apt install -y pipx && pipx install pyinfra && pipx ensurepath   # then reopen the terminal
docker login localhost:8085                                           # once, to be able to pull
cd /mnt/c/Users/khadi/covoit-iset-zaghouan                            # the folder with docker-compose.yml and .env
pyinfra @local deploy.py
```

`deploy.py` does this in order:
1. checks Docker;
2. runs `docker pull` on the latest image;
3. runs `docker compose up -d --no-build application`, which restarts the app with the new image;
4. waits until `/health` answers (90 s maximum, and shows the logs on failure);
5. removes the old images.

For the simple registry, run `IMAGE=localhost:5000/covoit-iset:latest pyinfra @local deploy.py`. Without pyinfra, `./deploy.sh` does the same thing.

✅ **Deliverables:** Harbor contains an image pushed by the CI · running `deploy.py` updates the container.

---

## Step 5: Monitoring and end-to-end validation

**5.1 Uptime Kuma:**
```bash
docker run -d \
  --name uptime-kuma \
  --restart unless-stopped \
  -p 3001:3001 \
  -v uptime-kuma-data:/app/data \
  louislam/uptime-kuma:1
```

1. Open **http://localhost:3001** and create the admin account.
2. Add a monitor of type **HTTP(s)**:
   - URL: **`http://host.docker.internal:3000/health`**. Inside the Kuma container, `localhost` means Kuma itself, not your PC. `host.docker.internal` is Docker Desktop's name for your PC.
   - Interval: **60 s**. Retries: **2**.
3. Notifications (Telegram, Discord, email) are optional and free.

`/health` also checks **the database**. If MySQL stops, it answers 503 and Kuma turns red.

**5.2 End-to-end test:**
```bash
./demo.sh            # 1-2. changes "ok" → "healthy", new branch, commit, push
# 3. Open the link it prints, create the Merge Request, wait for the green test job, merge
# 4. Watch the pipeline on main: test → build → push to Harbor
pyinfra @local deploy.py          # 5. deploy
curl http://localhost:3000/health # 6. → {"status":"healthy","version":"<new commit>",...}
```

The `version` field proves that the running container is the one built from **your** commit.

**5.3 Watch the monitoring:**
```bash
docker compose stop application    # Kuma turns red within a minute
docker compose start application   # back to green
docker compose stop base           # the app answers 503 "base":"erreur": red as well
docker compose start base
```

✅ **Deliverables:** Uptime Kuma running · one full cycle done (change → CI build → deploy → seen by the monitoring).

---

## Step 6: Documentation and wrap-up

- **`README.md`** (in the zip): architecture, pipeline flow, how to run, tests.
- **`demo.sh`**: the full loop in 30 seconds. Run it twice and it switches back to `"ok"`, so you can repeat the demo.
- **Presentation**: in a format and language of your choice, with the link to your project, for example `http://localhost:8929/root/covoit-iset-zaghouan` plus the GitHub link.

### Final checklist

| Deliverable | How to check | Files |
|---|---|---|
| GitLab repo with `main` protected | A direct `git push gitlab main` is refused; only MRs work | Settings › Protected branches |
| Dockerfile + docker-compose.yml | `docker compose up` → `/health` answers | `Dockerfile`, `docker-compose.yml`, `.dockerignore` |
| `.gitlab-ci.yml` | A pipeline on every push | `.gitlab-ci.yml` |
| Harbor registry | The `covoit` project contains `covoit-iset:<commit>` | CI/CD variables |
| `deploy.py` | Updates the container, and `/health` shows the new version | `deploy.py` (`deploy.sh` without pyinfra) |
| Uptime Kuma | The dashboard shows the app online | — |
| README.md | Architecture and instructions | `README.md` |

### Common pitfalls (from the guide, checked against your setup)

1. **Jobs stay pending** → the runner tag isn't `local`. Check `sudo gitlab-runner list`.
2. **`permission denied … docker.sock`** in a job → run `sudo usermod -aG docker gitlab-runner`, then `sudo gitlab-runner restart`.
3. **TLS errors when pushing to Harbor** → the `https:` block in `harbor.yml` is still active.
4. **GitLab is slow to start** → 3 to 5 minutes is normal. Follow it with `docker logs -f gitlab`.
5. **Port 8080 is busy** → that's Oracle on your PC. Use the ports in this guide (8929, 8085, 3000, 3001).
6. **No Kubernetes** → Docker Compose and scripts are enough.
7. **Secrets** → `.env` is in `.gitignore`, and the Harbor password goes in **masked** GitLab variables.
