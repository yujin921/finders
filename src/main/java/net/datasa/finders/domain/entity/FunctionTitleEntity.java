package net.datasa.finders.domain.entity;

import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "function_title")
public class FunctionTitleEntity {
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "function_title_id")
	private Integer functionTitleId;
    
    @Column(name = "title_name", nullable = false, length = 500)
    private String titleName;
    
    @Column(name = "function_processivity", nullable = false, columnDefinition = "DEFAULT '0%'")
    private String functionProcessivity;

    // TaskManagementEntity와의 관계 설정
    @OneToMany(mappedBy = "functionTitleEntity")
    private List<TaskManagementEntity> tasks;
    
    @Override
    public String toString() {
        return "FunctionTitleEntity{" +
                "functionTitleId=" + functionTitleId +
                ", titleName='" + titleName + '\'' +
                ", taskCount=" + (tasks != null ? tasks.size() : 0) +
                '}';
    }
    
}
