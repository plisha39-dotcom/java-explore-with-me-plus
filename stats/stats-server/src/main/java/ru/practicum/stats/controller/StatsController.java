package ru.practicum.stats.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.stats.service.HitService;

@RestController
public class StatsController {
    private final HitService hitService;

    public StatsController(HitService hitService) {
        this.hitService = hitService;
    }

    @PostMapping("/hit")
    @ResponseStatus(HttpStatus.CREATED)
    public void save(@RequestBody EndpiontHit endpiontHit) {
        hitService.save(endpiontHit);
    }
}
