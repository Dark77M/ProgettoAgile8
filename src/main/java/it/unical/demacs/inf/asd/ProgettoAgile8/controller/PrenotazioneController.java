package it.unical.demacs.inf.asd.ProgettoAgile8.controller;


import it.unical.demacs.inf.asd.ProgettoAgile8.core.Filtro;
import it.unical.demacs.inf.asd.ProgettoAgile8.dao.NotificaDAO;
import it.unical.demacs.inf.asd.ProgettoAgile8.dto.DottoreDTO;
import it.unical.demacs.inf.asd.ProgettoAgile8.dto.NotificaDTO;
import it.unical.demacs.inf.asd.ProgettoAgile8.dto.PazienteDTO;
import it.unical.demacs.inf.asd.ProgettoAgile8.dto.PrenotazioneDTO;
import it.unical.demacs.inf.asd.ProgettoAgile8.entities.Notifica;
import it.unical.demacs.inf.asd.ProgettoAgile8.entities.Paziente;
import it.unical.demacs.inf.asd.ProgettoAgile8.entities.Prenotazione;
import it.unical.demacs.inf.asd.ProgettoAgile8.service.NotificaService;
import it.unical.demacs.inf.asd.ProgettoAgile8.service.PrenotazioneService;
import it.unical.demacs.inf.asd.ProgettoAgile8.utility.SendEmail;
import org.apache.tomcat.jni.Local;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/restex")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class PrenotazioneController {

    @Autowired
    private PrenotazioneService prenotazioneService;

    @Autowired
    private NotificaService notificaService;

    @PostMapping(path = "/prenotazioniByPaziente")
    public ResponseEntity<List<PrenotazioneDTO>> getPrenotazioniByPaziente(@RequestBody PazienteDTO paziente){
        List<PrenotazioneDTO> lista = prenotazioneService.getPrenotazioniByPaziente(paziente);
        return ResponseEntity.ok(lista);
    }

    @PostMapping(path = "/prenotazioniByDoctor")
    public ResponseEntity<List<PrenotazioneDTO>> getPrenotazioniByDoctor(@RequestBody Filtro filtro){
        DottoreDTO dottore = filtro.getDottore();
        LocalDateTime date = filtro.getData();
        List<PrenotazioneDTO> lista = prenotazioneService.getPrenotazioniByDoctor(dottore,date, true);
        return ResponseEntity.ok(lista);
    }

    @PostMapping(path = "/richiesteByDoctor")
    public ResponseEntity<List<PrenotazioneDTO>> getRichiesteByDoctor(@RequestBody DottoreDTO dottore){
        List<PrenotazioneDTO> lista = prenotazioneService.getRichiesteByDoctor(dottore, false);
        return ResponseEntity.ok(lista);
    }


    @GetMapping(path = "/urgentiNonAccettati")
    public ResponseEntity<List<PrenotazioneDTO>> getUrgentiNonConfermate(){
       List<PrenotazioneDTO> lista = prenotazioneService.getUrgentiNonConfermate();
        return ResponseEntity.ok(lista);
    }

    @PostMapping(path = "/urgentiNonAccettatiByDoctor")
    public ResponseEntity<List<PrenotazioneDTO>> getUrgentiNonConfermateByDoctor(@RequestBody DottoreDTO dottore){
        List<PrenotazioneDTO> lista = prenotazioneService.getUrgentiNonConfermateByDoctor(dottore);
        return ResponseEntity.ok(lista);
    }


    @GetMapping(path = "/inAttesa")
    public ResponseEntity<List<PrenotazioneDTO>> getInAttesa(){
        List<PrenotazioneDTO> lista = prenotazioneService.getInAttesa();
        return ResponseEntity.ok(lista);
    }

    @GetMapping(path = "/accettati")
    public ResponseEntity<List<PrenotazioneDTO>> getAccettati(){
        List<PrenotazioneDTO> lista = prenotazioneService.getAccettate();
        return ResponseEntity.ok(lista);
    }

    @PostMapping(path = "/prenotazione")
    public ResponseEntity<PrenotazioneDTO> add(@RequestBody PrenotazioneDTO prenotazione){
        PrenotazioneDTO p = prenotazioneService.addPrenotazione(prenotazione);
        return ResponseEntity.ok(p);
    }

    @DeleteMapping(path = "/prenotazione/{id}")
    public HttpStatus delete(/*@RequestBody PrenotazioneDTO prenotazione*/@PathVariable Long id){
        System.out.println("deleete prenotazione");
        Prenotazione p = prenotazioneService.getById(id);
        Notifica notifica = new Notifica();
        Paziente paziente = new Paziente();
        paziente.setId(p.getPaziente().getId());
        notifica.setPaziente(paziente);
        notifica.setVista(false);
        String testo="La prenotazione è annullata.";
        notifica.setTesto(testo);
        notifica.setSegretaria(true);
        notificaService.save(notifica);

        prenotazioneService.deletePrenotazione(id);
        SendEmail.getInstance().sendMailDelete(/*prenotazione.getPaziente().getEmail()*/ "niko97142@gmail.com");
        return HttpStatus.OK;
    }

    @PutMapping(path =  "/prenotazione")
    public ResponseEntity<PrenotazioneDTO> update(@RequestBody PrenotazioneDTO prenotazione){
        Prenotazione prenotazioneVecchia = prenotazioneService.getById(prenotazione.getId());
        if(prenotazione.isConfermato() == prenotazioneVecchia.isConfermato() && prenotazione.getData_visita()!=null){
            if(!prenotazione.getData_visita().equals(prenotazioneVecchia.getData_visita())) {
                System.out.println("entro quiii");
                String testo="La data della sua prenotazione ha subito una variazione da "
                        +prenotazioneVecchia.getData_visita().plusHours(1).toString()+" a "+
                        prenotazione.getData_visita().plusHours(1).toString()+".";
                String oggetto="Variazione data prenotazione";
                SendEmail.getInstance().sendMail(oggetto,testo, "niko97142@gmail.com");

                Notifica notifica = new Notifica();
                Paziente p = new Paziente();
                p.setId(prenotazione.getPaziente().getId());
                notifica.setPaziente(p);
                notifica.setVista(false);
                notifica.setTesto(testo);
                notifica.setSegretaria(false);
                notificaService.save(notifica);
            }
        }
        if(prenotazione.isConfermato()==false && prenotazioneVecchia.isConfermato()==true){
            String testo="La sua prenotazione stabilita in data "+prenotazioneVecchia.getData_visita().plusHours(1)
                    +" è stata annullata, riceverà un altra email quando verrà riconfermata.";

            String oggetto="Prenotazione annullata";
            SendEmail.getInstance().sendMail(oggetto,testo, "niko97142@gmail.com");
            Notifica notifica = new Notifica();
            Paziente p = new Paziente();
            p.setId(prenotazione.getPaziente().getId());
            notifica.setPaziente(p);
            notifica.setVista(false);
            notifica.setTesto(testo);
            notifica.setSegretaria(false);
            notificaService.save(notifica);
        }
        if(prenotazione.isConfermato()==true && prenotazioneVecchia.isConfermato()==false){
            String testo="La sua prenotazione è stata confermata per la data "+prenotazione.getData_visita().plusHours(1);

            String oggetto="Prenotazione Confermata";
            SendEmail.getInstance().sendMail(oggetto,testo, "niko97142@gmail.com");
            Notifica notifica = new Notifica();
            Paziente p = new Paziente();
            p.setId(prenotazione.getPaziente().getId());
            notifica.setPaziente(p);
            notifica.setVista(false);
            notifica.setTesto(testo);
            notifica.setSegretaria(false);
            notificaService.save(notifica);

        }
        PrenotazioneDTO prenotazioneNuova = prenotazioneService.updatePrenotazione(prenotazione);
        return ResponseEntity.ok(prenotazioneNuova);
    }


}
