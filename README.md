# Jenkins Job DSL - Flask Docker Monitor

## Setup

1. **Replace `your_dockerhub` with your real docker repo** in `jobs.groovy`

2. **On Jenkins, create credentials:**
   - Go to Manage Jenkins → Manage Credentials
   - Add Username/Password credential
   - **ID = `docker-hub-credentials`**
   - Enter your Docker Hub username and password

## Usage

1. Run `seed_job` to create pipeline jobs
2. Run `flask_docker_build` 
3. Run `nginx_proxy_build`
4. Run `deploy_and_test`

## Access

Flask app: **http://localhost:8082**

Shows list of running Docker containers.
