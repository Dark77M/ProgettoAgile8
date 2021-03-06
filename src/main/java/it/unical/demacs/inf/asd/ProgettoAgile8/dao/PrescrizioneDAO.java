package it.unical.demacs.inf.asd.ProgettoAgile8.dao;

import it.unical.demacs.inf.asd.ProgettoAgile8.entities.Prescrizione;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.swing.text.Document;
import java.util.List;

@Repository
public interface PrescrizioneDAO extends JpaRepository<Prescrizione, Long>, JpaSpecificationExecutor<Prescrizione> {

    List<Prescrizione> findAll();

}
