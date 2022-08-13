# Kubernetes Deployment

This directory contains resources for deploying Search and Record API to a Kubernetes cluster. 

## Requirements
- [Kustomize](https://kubectl.docs.kubernetes.io/installation/kustomize/) for generating Kubernetes manifests and managing environment-specific configurations.
- [envsubst](https://linux.die.net/man/1/envsubst) for generating customization files.

## File Structure
This folder consists of a Kustomize base layer and two patch layers: for local development (`dev`) and cloud deployment (`cloud`). 
The files below are required for deployment:

 ```
 k8s:
  ├── base
  │    ├── kustomization.yaml
  │    ├── europeana.user.properties#
  │    └── google_cloud_credentials.json#
  └── overlays
       ├── cloud
       │     ├── deployment_patch.properties.yaml#
       │     ├── deployment_patch.properties.yaml.template
       │     ├── hpa.properties.yaml#
       │     ├── hpa.properties.yaml.template
       │     ├── ingress.properties.yaml#
       │     ├── ingress.properties.yaml.template
       │     ├── service.yaml
       │     ├── kustomization.yaml
       └──  dev
             ├── nodeport.yaml
             ├── deployment_patch.yaml
             └── kustomization.yaml 
 ```
_# indicates a file not in version control_

### File naming scheme
File names have the following structure:

- `*.properties.yaml.template` contain environment variables that need to be substituted while generating the corresponding YAML file
- `*.properties.yaml` are generated via a template file. These contain configurable settings and are not checked in to git
- `*_patch*.yaml` "patch" resources created in the base layer. These don't have to configurable, eg. `k8s/dev/deployment_patch.yaml`
- `*.yaml` are plain Kubernetes manifests that don't require any customization.

## Deployment Instructions
For both environments:
- Copy a valid properties to the `k8s/base` directory, and rename it to `europeana.user.properties`. Ensure at least [one route](https://github.com/europeana/api2/blob/6b0a64770f07a6a45a65f3c17b18bdcbea9010f4/api2-war/src/main/resources/europeana.properties#L5) matches the URL hostname through which the app will be accessed.
- Copy `google_cloud_credentials.json` to the `k8s/base`

### Local Deployment
- Build the API while in the project root directory: `mvn clean package`
- Build a docker image from the same directory: `docker build -t europeana/search-api .`. 
- If required, load the image into your local Kubernetes cluster. 
- To view the customised Kubernetes manifests run `kustomize build k8s/overlays/dev`
- Apply the manifests to the cluster: `kubectl apply -k k8s/overlays/dev`

Run `kubectl get deployment/search-api-deployment` to view the deployment's status. 
After deploying successfully the app will be available on `<cluster_host>:30000`, where `<cluster_host>` :
- is "localhost" for Docker Desktop and Kind
- can be retrieved by running `minikube ip` if using a Minikube cluster

### IBM Cloud Deployment
The `k8s/overlay/cloud` directory contains templates to be used for generating YAML files for Kustomize. 
The following environment variables are required by the templates:

| ENVIRONMENT VARIABLE | DESCRIPTION                                                                                     | USED BY                                                                         |
|----------------------|-------------------------------------------------------------------------------------------------|---------------------------------------------------------------------------------|
| MIN_REPLICAS         | Minimum number of replicas to run                                                               | `deployment_patch.properties.yaml.template`<br/> `hpa_properties.yaml.template` |
| MAX_REPLICAS         | Maximum number of replicas to run when auto scaling                                             | `hpa_properties.yaml.template`                                                  |
| MEMORY_REQUEST       | Amount of memory in megabytes to request during application deployment                          | `deployment_patch.properties.yaml.template`                                     |
| MEMORY_LIMIT         | Application memory limit in megabytes                                                           | `deployment_patch.properties.yaml.template`                                     |
| CPU_REQUEST          | Amount of CPU to request during application deployment, in milliCPU form. <br>1000 = 1 CPU core | `deployment_patch.properties.yaml.template`                                     |
| CPU_LIMIT            | Max CPU allocation for app, in milliCPU form. <br> 1000 = 1 CPU core                            | `deployment_patch.properties.yaml.template`                                     |
| SEARCH_API_HOSTNAME  | Ingress hostname, ie. a FQDN to be used for accessing the app                                   | `ingress.properties.yaml.template`                                              |

These variables can be provided either via a `.env` file, Jenkins job configuration, or the Linux `export`
command.

Generate the customization files with the following commands (while in `./k8s/overlays/cloud`, but adjust paths accordingly):

```
envsubst < ingress.properties.yaml.template > ingress.properties.yaml
envsubst < hpa.properties.yaml.template > hpa.properties.yaml
envsubst < deployment_patch.properties.yaml.template > deployment_patch.properties.yaml
```
The YAML files created by these commands are used by Kustomize.

To view the customised Kubernetes manifests run `kustomize build k8s/overlays/cloud`. 

The manifests can then be applied to the cluster by running `kubectl apply -k k8s/overlays/cloud`
