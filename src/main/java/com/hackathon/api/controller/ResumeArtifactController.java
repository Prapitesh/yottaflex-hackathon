package com.hackathon.api.controller;
import com.hackathon.api.dto.request.resume.ResumeArtifactRequest;
import com.hackathon.api.dto.response.resume.ResumeArtifactResponse;
import com.hackathon.api.service.ResumeArtifactService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.net.URI;
import java.util.List;
@RestController @RequestMapping("/api/v1/resumes")
public class ResumeArtifactController {
 private final ResumeArtifactService service; public ResumeArtifactController(ResumeArtifactService s){service=s;}
 @PostMapping public ResponseEntity<ResumeArtifactResponse> create(@Valid @RequestBody ResumeArtifactRequest r){ResumeArtifactResponse x=service.create(r);return ResponseEntity.created(URI.create("/api/v1/resumes/"+x.id())).body(x);}
 @GetMapping("/{id}") public ResumeArtifactResponse get(@PathVariable String id){return service.getById(id);}
 @GetMapping("/candidate/{candidateId}") public List<ResumeArtifactResponse> byCandidate(@PathVariable String candidateId){return service.getByCandidate(candidateId);}
}