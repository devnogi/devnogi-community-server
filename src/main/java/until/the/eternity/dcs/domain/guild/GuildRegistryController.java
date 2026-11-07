package until.the.eternity.dcs.domain.guild;

import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class GuildRegistryController {
    private final GuildRegistry registry;

    public record Registration(String name, String description) {}

    public record Review(String decision, String reason) {}

    public record Master(long userId) {}

    public record Role(String role) {}

    @GetMapping("/api/guilds/registrations")
    public List<Map<String, Object>> mine() {
        return registry.mine();
    }

    @PostMapping("/api/guilds/registrations")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void submit(@RequestBody Registration request) {
        registry.submit(request.name(), request.description());
    }

    @DeleteMapping("/api/guilds/registrations/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cancel(@PathVariable long id) {
        registry.cancel(id);
    }

    @GetMapping("/api/admin/guilds/registrations")
    public List<Map<String, Object>> queue() {
        return registry.queue();
    }

    @PostMapping("/api/admin/guilds/registrations/{id}/review")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void review(@PathVariable long id, @RequestBody Review review) {
        registry.review(id, review.decision(), review.reason());
    }

    @PutMapping("/api/admin/guilds/{id}/master")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void master(@PathVariable int id, @RequestBody Master master) {
        registry.master(id, master.userId());
    }

    @GetMapping("/api/guilds/{id}/members")
    public List<Map<String, Object>> members(@PathVariable int id) {
        return registry.members(id);
    }

    @PutMapping("/api/guilds/{id}/members/{userId}/role")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void role(@PathVariable int id, @PathVariable long userId, @RequestBody Role role) {
        registry.role(id, userId, role.role());
    }

    @DeleteMapping("/api/guilds/{id}/members/me")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void leave(@PathVariable int id) {
        registry.leave(id);
    }
}
