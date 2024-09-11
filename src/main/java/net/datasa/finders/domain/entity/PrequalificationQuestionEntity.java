package net.datasa.finders.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "prequalification_questions")
public class PrequalificationQuestionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "question_id")
    Integer questionId;

    @ManyToOne
    @JoinColumn(name = "project_num", referencedColumnName = "project_num")
    ProjectPublishingEntity projectPublishingEntity;

    @Column(name = "question_text", nullable = false, length = 2000)
    String questionText;
}
