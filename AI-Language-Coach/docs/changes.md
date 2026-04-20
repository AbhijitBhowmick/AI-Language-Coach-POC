# Platform-First Architecture: Changes Summary

**Date:** April 14, 2026  
**Reason:** Enable 1M+ concurrent users with Virtual Threads + Distributed Infrastructure + Multi-Tenant Authentik IAM

---

## What Changed & Why

| Change | Why | Impact |
|--------|-----|-------|
| **Single Module → Multi-Module** | Generic platform logic can be reused for Stock Analytics, IoT, etc. | Code reuse, faster development |
| **Java 21 Virtual Threads (Loom)** | Handle 1M concurrent users without CPU crash. Each user = 1 virtual thread, not OS thread. | 10x more concurrent connections |
| **@ConditionalOnProperty** | Switch between "coach" and "analytics" modes without code changes | One codebase, multiple services |
| **Local → Distributed Config** | Mac connects to HP Laptop (192.168.0.18) for Valkey/Qdrant | Shared infrastructure, lower latency (when on same network) |
| **Hardcoded defaults → PlatformConfig** | All IPs/ports configurable via application.yml | No recompilation for deployment changes |
| **ZRAM Optimization** | HP Laptop uses ZRAM for memory compression — allows more virtual threads | Prevents OOM on 8GB machines |
| **HP Laptop as Infra Node** | Dedicated PostgreSQL + Valkey + Qdrant — app runs on Mac | Better resource management |
| **TTL-based Cache** | Valkey 365-day TTL for profiles, 2-hour for diagnostic state | Memory efficiency |
| **Google OAuth → Authentik OIDC** | Replace Google OAuth with Authentik as Identity Provider | Multi-tenant SSO with separate user databases |
| **Single-tenant → Multi-tenant** | Authentik native multi-tenancy with separate PostgreSQL schemas | Language Coach + New App as separate tenants |

---

## Architecture Comparison

### Before (Single Monolith)
```
Mac (Local Only)
├── Java App (Spring Boot)
├── PostgreSQL (localhost:5432)
├── Valkey (localhost:6379)
└── Qdrant (localhost:6333)
```

### After (Platform-First)
```
Mac (Application)
├── Java App (Virtual Threads)
└── Connects to HP Laptop → PostgreSQL + Valkey + Qdrant

HP Laptop (192.168.0.18) - Infrastructure Node
├── PostgreSQL (192.168.0.18:5432)
├── Valkey (192.168.0.18:6379)
└── Qdrant (192.168.0.18:6333)
```

---

## Virtual Threads: The 1M User Solution

### Problem
- **Traditional Java threads**: Each user = 1 OS thread (~1MB stack) 
- **1M users** = 1M OS threads = 1TB RAM ❌ Impossible

### Solution: Virtual Threads (Java 21 / Project Loom)
- **Each user** = 1 virtual thread (~256 bytes stack)
- **1M users** = 256MB RAM ✅ Feasible
- **No CPU context switching** — OS doesn't manage them
- **Automatic scaling** — Create millions, system handles it

```java
// Virtual Threads are enabled by default in Java 21
// No code changes needed!
Thread thread = Thread.startVirtualThread(() -> {
    // Handle user request
});
```

---

## ZRAM Optimization (HP Laptop)

### What is ZRAM?
- Compressed RAM swap on Linux
- Acts as in-memory swap
- Allows MORE virtual threads

### Enable on Lubuntu
```bash
# Check if enabled
cat /sys/block/zram0/comp_algorithm

# Enable
sudo modprobe zram num_devices=2
echo lz4 | sudo tee /sys/block/zram0/comp_algorithm
sudo zramctl --reset /dev/zram0
```

---

## Environment Configuration

### Local Mode (Mac runs all services)
```bash
export INFRASTRUCTURE_MODE=local  # Default
./mvnw spring-boot:run
```

### Distributed Mode (Connects to HP Laptop)
```bash
export INFRASTRUCTURE_MODE=distributed
export VALKEY_HOST=192.168.0.18
export DB_HOST=192.168.0.18
./mvnw spring-boot:run
```

---

## Deployment Options

| Mode | Infrastructure | Use Case |
|------|----------------|----------|
| `local` | Mac runs PostgreSQL + Valkey | Development |
| `distributed` | HP Laptop runs PostgreSQL + Valkey | Production |
| `ampere` | Oracle Cloud Ampere (ARM64) | Cloud Production |

---

## HDD Latency Considerations

| Storage | Sequential IOPS | Random IOPS | Latency |
|---------|----------------|-------------|---------|
| HDD (7200RPM) | ~100 | ~50 | 8-12ms |
| SSD (NVMe) | ~100K | ~50K | 0.1ms |
| Valkey (RAM) | N/A | N/A | 0.1ms ✅ |

**Recommendation**: Run Valkey in RAM (HP Laptop with 8GB+ RAM)

---

## Testing the Mac-to-HP Bridge

```bash
# 1. Verify HP Laptop is reachable
ping 192.168.0.18

# 2. Test Valkey from Mac
nc -zv 192.168.0.18 6379
# Expected: Connection succeeded

# 3. Test PostgreSQL from Mac  
nc -zv 192.168.0.18 5432
# Expected: Connection succeeded

# 4. Run application in distributed mode
INFRASTRUCTURE_MODE=distributed ./mvnw spring-boot:run
```

---

## Next Steps

- [ ] Test Mac → HP Laptop connection
- [ ] Run container-compose on HP Laptop
- [ ] Switch to distributed mode
- [ ] Verify 1M user capacity (load test)