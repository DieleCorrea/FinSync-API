package com.financas.tema1.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "transactions")
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private String description;
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    private Category category;

    private LocalDate date;
    private String source;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    public Transaction() {}

    public Transaction(String description, BigDecimal amount, Category category, LocalDate date, String source, User user) {
        this.description = description;
        this.amount = amount;
        this.category = category;
        this.date = date;
        this.source = source;
        this.user = user;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public Category getCategory() { return category; }
    public void setCategory(Category category) { this.category = category; }
    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }
    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
}