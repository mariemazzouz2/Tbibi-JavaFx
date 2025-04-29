package models;

import models.Utilisateur;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Reponse {
    private Integer id;
    private models.Question question;
    private Utilisateur medecin;
    private String contenu;
    private LocalDateTime dateReponse;
    private List<Vote> votes;

    public Reponse() {
        this.dateReponse = LocalDateTime.now();
        this.votes = new ArrayList<>();
    }


    // Getters and Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public models.Question getQuestion() {
        return question;
    }

    public void setQuestion(models.Question question) {
        this.question = question;
    }

    public Utilisateur getMedecin() {
        return medecin;
    }

    public void setMedecin(Utilisateur medecin) {
        this.medecin = medecin;
    }

    public String getContenu() {
        return contenu;
    }

    public void setContenu(String contenu) {
        this.contenu = contenu;
    }

    public LocalDateTime getDateReponse() {
        return dateReponse;
    }

    public void setDateReponse(LocalDateTime dateReponse) {
        this.dateReponse = dateReponse;
    }

    public List<Vote> getVotes() {
        return votes;
    }

    public void setVotes(List<Vote> votes) {
        this.votes = votes;
    }

    public void addVote(Vote vote) {
        if (!this.votes.contains(vote)) {
            this.votes.add(vote);
            vote.setReponse(this);
        }
    }

    public void removeVote(Vote vote) {
        if (this.votes.remove(vote)) {
            vote.setReponse(null);
        }
    }
}
