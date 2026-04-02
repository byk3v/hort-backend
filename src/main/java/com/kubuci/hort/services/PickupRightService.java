package com.kubuci.hort.services;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kubuci.hort.dto.NewPermissionCollectorInlineDto;
import com.kubuci.hort.dto.NewPermissionRequest;
import com.kubuci.hort.dto.NewPermissionWeeklyAllowedFrom;
import com.kubuci.hort.dto.PermissionViewDto;
import com.kubuci.hort.dto.PickupRightCreateRequest;
import com.kubuci.hort.dto.PickupRightDto;
import com.kubuci.hort.enums.CollectorType;
import com.kubuci.hort.enums.PermissionStatus;
import com.kubuci.hort.enums.PermissionType;
import com.kubuci.hort.models.Collector;
import com.kubuci.hort.models.Person;
import com.kubuci.hort.models.PickupRight;
import com.kubuci.hort.models.SelfDismissal;
import com.kubuci.hort.models.Student;
import com.kubuci.hort.repositories.CollectorRepository;
import com.kubuci.hort.repositories.PersonRepository;
import com.kubuci.hort.repositories.PickupRightRepository;
import com.kubuci.hort.repositories.SelfDismissalRepository;
import com.kubuci.hort.repositories.StudentRepository;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PickupRightService {

    private final PickupRightRepository repo;
    private final SelfDismissalRepository selfDismissalRepository;
    private final StudentRepository studentRepo;
    private final CollectorRepository collectorRepo;
    private final PersonRepository personRepository;

    @Transactional
    public UUID create(PickupRightCreateRequest req) {
        var student = studentRepo.findById(req.studentId())
                .orElseThrow(() -> new EntityNotFoundException("Student not found: " + req.studentId()));
        var collector = collectorRepo.findById(req.collectorId())
                .orElseThrow(() -> new EntityNotFoundException("Collector not found: " + req.collectorId()));

        var pr = new PickupRight();
        pr.setStudent(student);
        pr.setCollector(collector);
        pr.setType(req.type());
        pr.setValidFrom(req.validFrom());
        pr.setValidUntil(req.validUntil());
        pr.setStatus(PermissionStatus.ACTIVE);
        return repo.save(pr)
                .getId();
    }

    @Transactional
    public void revoke(UUID id) {
        var pr = repo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("PickupRight not found: " + id));
        if (pr.getStatus() == PermissionStatus.REVOKED)
            return;
        pr.setStatus(PermissionStatus.REVOKED);
        repo.save(pr);
    }

    @Transactional(readOnly = true)
    public List<PickupRightDto> listByStudent(UUID studentId) {
        return repo.findByStudent_Id(studentId)
                .stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PickupRightDto> listByCollector(UUID collectorId) {
        return repo.findByCollector_Id(collectorId)
                .stream()
                .map(this::toDto)
                .toList();
    }

    private PickupRightDto toDto(PickupRight p) {
        return new PickupRightDto(p.getId(), p.getStudent()
                .getId(),
                p.getCollector()
                        .getId(),
                p.getType(), p.getValidFrom(), p.getValidUntil(), p.getStatus());
    }

    @Transactional(readOnly = true)
    public List<PermissionViewDto> listPermissions(String statusFilter) {

        // 1. Buscar PickupRight
        List<PickupRight> pickupRights = statusFilter.equalsIgnoreCase("ALL")
                ? repo.findAllWithStudentAndCollector()
                : repo.findByStatusWithStudentAndCollector(PermissionStatus.ACTIVE);

        // 2. Buscar SelfDismissal
        List<SelfDismissal> selfDismissals = statusFilter.equalsIgnoreCase("ALL")
                ? selfDismissalRepository.findAllWithStudent()
                : selfDismissalRepository.findByStatusWithStudent(PermissionStatus.ACTIVE);

        // 3. Mapear ambos a PermissionViewDto
        List<PermissionViewDto> list = new java.util.ArrayList<>();

        for (PickupRight pr : pickupRights) {
            var s = pr.getStudent();
            var sp = s.getPerson();
            var gName = s.getGroup() != null
                    ? s.getGroup()
                            .getName()
                    : null;

            var c = pr.getCollector();
            var cp = c.getPerson();

            list.add(new PermissionViewDto(pr.getId(), "COLLECTOR", s.getId(), sp.getFirstName(), sp.getLastName(),
                    gName, c.getId(), cp.getFirstName(), cp.getLastName(), cp.getPhone(), pr.getValidFrom() != null
                            ? pr.getValidFrom()
                                    .toString()
                            : null,
                    pr.getValidUntil() != null
                            ? pr.getValidUntil()
                                    .toString()
                            : null,
                    pr.getAllowedFromTime() != null
                            ? pr.getAllowedFromTime()
                                    .toString()
                            : null,
                    pr.getStatus()
                            .name()));
        }

        for (SelfDismissal sd : selfDismissals) {
            var s = sd.getStudent();
            var sp = s.getPerson();
            var gName = s.getGroup() != null
                    ? s.getGroup()
                            .getName()
                    : null;

            list.add(new PermissionViewDto(sd.getId(), "SELF_DISMISSAL", s.getId(), sp.getFirstName(), sp.getLastName(),
                    gName, null, // collectorId
                    null, // collectorFirstName
                    null, // collectorLastName
                    null, // collectorPhone
                    sd.getValidFrom() != null
                            ? sd.getValidFrom()
                                    .toString()
                            : null,
                    sd.getValidUntil() != null
                            ? sd.getValidUntil()
                                    .toString()
                            : null,
                    sd.getAllowedFromTime() != null
                            ? sd.getAllowedFromTime()
                                    .toString()
                            : null,
                    sd.getStatus()
                            .name()));
        }

        // 4. Ordenar (opcional) por alumno, luego por validFrom desc
        list.sort((a, b) -> {
            int byStudent = (a.studentLastName() + a.studentFirstName())
                    .compareToIgnoreCase(b.studentLastName() + b.studentFirstName());
            if (byStudent != 0) {
                return byStudent;
            }
            return b.validFrom()
                    .compareToIgnoreCase(a.validFrom());
        });

        return list;
    }

    @Transactional
    public void createPermission(NewPermissionRequest req) {

        Student student = studentRepo.findById(req.studentId())
                .orElseThrow(() -> new EntityNotFoundException("Student not found"));

        boolean isSelf = Boolean.TRUE.equals(req.canLeaveAlone());

        // Normalizar fechas
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime validFrom = req.validFrom() != null
                ? req.validFrom()
                : now;
        LocalDateTime validUntil = req.validUntil(); // puede ser null

        if (isSelf) {
            createSelfDismissal(student, req, validFrom, validUntil);
        }
        else {
            createPickupRight(student, req, validFrom, validUntil);
        }
    }

    private void createSelfDismissal(Student student, NewPermissionRequest req, LocalDateTime validFrom,
            LocalDateTime validUntil) {
        // allowedFromTime:
        // - Tagesvollmacht: req.allowedFromTime es una sola hora en ese día
        // - Dauervollmacht con weeklyAllowedFrom: OJO esto no cabe directo en nuestro
        // modelo actual
        //
        // Hoy la tabla `self_dismissal` sólo tiene UNA `allowed_from_time`.
        // Eso significa que para el caso "DAUER + canLeaveAlone + horario distinto por
        // día"
        // hay que definir:
        //
        // Opción simple (MVP): en este release guardamos un único fromTime común (ej.
        // el más temprano de la semana).
        // Opción larga: crear otra tabla tipo self_dismissal_weekday con day_of_week +
        // allowed_from_time.
        //
        // Implemente la version corta, tambien tengo que cargar estudiantes de verdad:
        // tomamos el horario más restrictivo (el más tarde) entre los días definidos.
        LocalTime allowedFromTime = null;

        if (req.kind()
                .equalsIgnoreCase("TAGES")) {
            allowedFromTime = req.allowedFromTime();
        }
        else {
            // DAUER
            if (req.weeklyAllowedFrom() != null) {
                allowedFromTime = latestDefinedTime(req.weeklyAllowedFrom());
            }
        }

        SelfDismissal sd = new SelfDismissal();
        sd.setStudent(student);
        sd.setValidFrom(validFrom);
        sd.setValidUntil(validUntil);
        sd.setAllowedFromTime(allowedFromTime);
        sd.setStatus(PermissionStatus.ACTIVE);

        selfDismissalRepository.save(sd);
    }

    private LocalTime latestDefinedTime(NewPermissionWeeklyAllowedFrom weekly) {
        // weekly.monday() etc. son Strings "HH:mm" o null
        // parseamos y nos quedamos con la más tarde (más restrictiva).
        return java.util.stream.Stream
                .of(weekly.monday(), weekly.tuesday(), weekly.wednesday(), weekly.thursday(), weekly.friday())
                .filter(java.util.Objects::nonNull)
                .map(t -> LocalTime.parse(t)) // "15:30" -> LocalTime
                .max(LocalTime::compareTo)
                .orElse(null);
    }

    private void createPickupRight(Student student, NewPermissionRequest req, LocalDateTime validFrom,
            LocalDateTime validUntil) {
        // 1. Crear / reutilizar collector
        NewPermissionCollectorInlineDto cReq = req.collector();
        if (cReq == null) {
            throw new IllegalArgumentException("Collector data required for non-self permission");
        }

        Collector collector = findOrCreateCollector(cReq);

        // 2. Crear PickupRight
        PickupRight pr = new PickupRight();
        pr.setStudent(student);
        pr.setCollector(collector);
        pr.setValidFrom(validFrom);
        pr.setValidUntil(validUntil);
        pr.setAllowedFromTime(req.allowedFromTime()); // puede ser null
        pr.setStatus(PermissionStatus.ACTIVE);
        pr.setType(req.kind()
                .equalsIgnoreCase("TAGES")
                        ? PermissionType.DAILY // si tienes este enum
                        : PermissionType.PERMANENT);
        pr.setMainCollector(false); // o true si quieres marcarlo manualmente ahora

        repo.save(pr);
    }

    private Collector findOrCreateCollector(NewPermissionCollectorInlineDto dto) {
        // Buscar collector existente por (firstName,lastName,phone)
        // Si no existe, crearlo con su Person.
        return collectorRepo.findMatch(dto.firstName(), dto.lastName(), dto.phone())
                .orElseGet(() -> {
                    Person person = new Person();
                    person.setFirstName(dto.firstName());
                    person.setLastName(dto.lastName());
                    person.setAddress(dto.address());
                    person.setPhone(dto.phone());
                    personRepository.save(person);

                    Collector c = new Collector();
                    c.setPerson(person);
                    c.setCollectorType(CollectorType.COLLECTOR); // asumiendo
                    return collectorRepo.save(c);
                });
    }
}
