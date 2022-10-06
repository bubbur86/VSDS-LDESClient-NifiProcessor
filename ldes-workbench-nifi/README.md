<<<<<<< HEAD
# LDES Workbench NiFi

A docker is provided that contains a NiFi instance (based on the apache/nifi docker) and the LDES Client processor NAR files. The docker doesn't contain any workflows, these can be uploaded with a [script](./ldes-client-wrappers/ldes-client-wrappers-nifi/ldes-workbench-nifi/scripts/make.sh)

```bash
./make.sh nifi
```s

This will upload the configured workflow to the docker.

Copy the .env.local.example to .env and change to appropriate values, in particular:
- `NIFI_API` the URL of the workbench docker NiFi API
- `NIFI_PROCESS_GROUP_JSON` the name of the workflow file
- `LDES_CLIENT_SINK_URL` the URL where members should be posted
- `LDES_SERVER_SIMULATOR_URL` the URL where fragments are fetched from
- `NIFI_LDES_SERVER_SIMULATOR_RUN_SCHEDULE` the time to wait between different triggers of the LdesClient



=======
>>>>>>> origin
# LDES Client wrappers

Contained here are modules that use the LDES client to replicate and synchronize an LDES.