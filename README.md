# LDES Client Bundle

An LDES client is an LDES component that reads and writes LDES data.
In the client workflow, multiple steps


## Modules

Currently there are 2 modules of interest: the LDES client and the NiFi wrapper
* [LDES Client](./ldes-client/README.md)
* [LDES Client wrappers](./ldes-client-wrappers/README.md)

The client contains a library that fetches fragments and extracts members from them. It also has a CLI to easily fetch a stream and print the fragments to the console.

The ldes-client-wrappers-nifi module is a NiFi processor that wraps around the client to take in an LDES and produce LDES members.
