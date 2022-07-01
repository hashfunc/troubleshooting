package io.hashfunc.troubleistiomongodb

import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/hit")
class HitController(
    private val hitRepository: HitRepository,
) {

    @PostMapping("")
    fun hit(): Hit {
        val hit = Hit()
        hitRepository.save(hit)
        return hit
    }
}
