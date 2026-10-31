package com.finsight.backend.tearsheet;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tearsheet")
public class TearSheetController {

    private final TearSheetService tearSheetService;

    public TearSheetController(TearSheetService tearSheetService) {
        this.tearSheetService = tearSheetService;
    }

    @GetMapping
    public TearSheetResponse getTearSheet(
            @RequestParam String company,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) String ticker
    ) {
        return tearSheetService.generateTearSheet(company, year, ticker);
    }
}