# Design-Sandbox Plugin Issues Tracker

## Critical Issues 🔴

### Security
- [ ] World creation not rate-limited per player
- [ ] No world size validation before copy
- [ ] Unsafe UUID to string conversion in world naming
- [ ] Directory path not sanitized in config
- [ ] No anti-grief protection system
- [ ] Static instance pattern in main plugin class unsafe
- [ ] Unsafe MultiverseCore casting in dependency check

### Data Loss Prevention
- [ ] No world backup before reset
- [ ] World deletion not properly synchronized
- [ ] No progress persistence for long operations
- [ ] No disk space check before world copy
- [ ] File operations not properly buffered
- [ ] No copy verification after world creation

### Exception Handling
- [ ] substring() in ExitCommand could throw IndexOutOfBoundsException
- [ ] CompletableFuture chain breaks on exception in WorldManager
- [ ] Exception swallowing in DesignCommand execute() loses original error
- [ ] No null checks in multiple critical locations
- [ ] Getter methods not null-safe in main plugin

## Major Issues 🟠

### Performance
- [ ] No caching for frequently accessed data
- [ ] Progress updates spam player's action bar
- [ ] Copy delay blocks thread pool
- [ ] No maximum concurrent world copies check
- [ ] Region calculation includes unnecessary regions
- [ ] No caching of calculated regions
- [ ] Memory usage not optimized for large worlds

### Functionality
- [ ] returnLocation stored but never used in EnterCommand
- [ ] World unload check runs too soon after teleport
- [ ] No proper completion handling for async world creation
- [ ] Game rules not loaded from config
- [ ] World border not properly centered
- [ ] World load doesn't verify world type
- [ ] No reload command support
- [ ] No world template support
- [ ] Missing WorldEdit integration
- [ ] No WorldGuard region support

### Command System
- [ ] Permission check hardcoded in DesignCommand
- [ ] Cooldown system doesn't persist across restarts
- [ ] No command queuing for long operations
- [ ] No command confirmation system
- [ ] No undo system
- [ ] Tab completion doesn't handle aliases
- [ ] No subcommand argument validation

## Minor Issues 🟡

### Configuration
- [ ] No config versioning system
- [ ] Missing config migration
- [ ] No config validation
- [ ] Game rules incomplete for 1.21.4
- [ ] Debug settings too simple
- [ ] Messages lack placeholder documentation
- [ ] No permission inheritance structure
- [ ] Command description lacks examples
- [ ] Usage strings not internationalized

### World Management
- [ ] Region calculation ignores world height
- [ ] No validation for negative radius
- [ ] Bit shift operations assume 512 block regions
- [ ] Distance check ignores Y axis
- [ ] Region filename format hardcoded
- [ ] totalFiles count wrong for empty regions
- [ ] Source world save not async

### Integration
- [ ] LuckPerms hook incomplete
- [ ] PWI detection lacks version check
- [ ] Missing version requirements for soft dependencies
- [ ] No proper Multiverse-Core world import
- [ ] PWI settings lack group support

### User Experience
- [ ] Message formatting doesn't handle plural/singular for time
- [ ] No progress updates during reset
- [ ] Success message sent before world actually ready
- [ ] Error messages lose original error cause
- [ ] ActionBar messages overlap
- [ ] No task cancellation support

## Resolved Issues ✅

*No issues have been resolved yet.*

## Notes
- Issues are categorized by severity and type
- Critical issues should be addressed first
- Some issues may be interconnected and require coordinated fixes
- Performance issues should be tested with profiling
- Security issues require thorough testing

## Progress Tracking
- Total Issues: 52
- Critical Issues: 13
- Major Issues: 17
- Minor Issues: 22
- Resolved Issues: 0

## Next Steps
1. Address critical security issues
2. Implement data loss prevention measures
3. Fix exception handling
4. Optimize performance bottlenecks
5. Enhance core functionality
6. Improve command system
7. Update configuration system
8. Polish user experience

## Recent Updates
*No updates yet - initial issue tracking created.* 