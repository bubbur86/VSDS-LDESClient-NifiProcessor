# LDES Client

This module contains the LDES client code that replicates and synchronize an LDES and keeps (non-persisted) state for that process.

Wrappers can call this module to do the actual work.

The LdesServiceImpl makes some assumptions to configure the service when the desired configuration is not passed:
- **ld+json** is assumed to be the data format of the source LDES
- **n-quads** is assumed to be the desired data format of the destination processor
- **3600 seconds** is assumed to be a reasonable interval to use to construct an expiration date for unconfigured fragments (reasonable for testing purposes).