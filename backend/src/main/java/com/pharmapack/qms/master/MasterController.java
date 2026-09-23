package com.pharmapack.qms.master;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/master")
public class MasterController {

    private final MaterialRepository materialRepository;
    private final EquipmentRepository equipmentRepository;
    private final LineRepository lineRepository;

    public MasterController(
            MaterialRepository materialRepository,
            EquipmentRepository equipmentRepository,
            LineRepository lineRepository) {

        this.materialRepository = materialRepository;
        this.equipmentRepository = equipmentRepository;
        this.lineRepository = lineRepository;
    }

    // =========================
    // MATERIAL
    // =========================

    @GetMapping("/materials")
    public List<Material> materials() {
        return materialRepository.findAll();
    }

    @PostMapping("/materials")
    @ResponseStatus(HttpStatus.CREATED)
    public Material material(@RequestBody Material material) {
        return materialRepository.save(material);
    }

    // =========================
    // EQUIPMENT
    // =========================

    @GetMapping("/equipment")
    public List<Equipment> equipment() {
        return equipmentRepository.findAll();
    }

    @PostMapping("/equipment")
    @ResponseStatus(HttpStatus.CREATED)
    public Equipment equipment(@RequestBody Equipment equipment) {
        return equipmentRepository.save(equipment);
    }

    // =========================
    // PACKAGING LINES
    // =========================

    @GetMapping("/lines")
    public List<PackagingLine> lines() {
        return lineRepository.findAll();
    }

    @PostMapping("/lines")
    @ResponseStatus(HttpStatus.CREATED)
    public PackagingLine line(@RequestBody PackagingLine line) {
        return lineRepository.save(line);
    }
}
