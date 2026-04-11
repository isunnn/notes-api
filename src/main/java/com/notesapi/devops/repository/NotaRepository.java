package com.notesapi.devops.repository;

import com.notesapi.devops.entity.Nota;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotaRepository extends JpaRepository<Nota, Long> {}