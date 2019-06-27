package br.com.rnegrao.testepoc.host;

import br.com.rnegrao.testepoc.application.service.TweetsService;
import br.com.rnegrao.testepoc.domain.model.CountHashtags;
import br.com.rnegrao.testepoc.domain.model.CountTweets;
import br.com.rnegrao.testepoc.domain.model.Top5FollowersTweets;
import br.com.rnegrao.testepoc.host.response.ErrorResponse;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Api(value = "Operacoes de pesquisa para os tweets")
@ApiResponses(value = {
        @ApiResponse(code = 401, message = "Não foi autorizado para realizar essa operação", response = ErrorResponse.class),
        @ApiResponse(code = 422, message = "Erro de negócio", response = ErrorResponse.class),
        @ApiResponse(code = 500, message = "Erro de infraestrutrua", response = ErrorResponse.class)})
@RestController
@RequestMapping("/tweets")
public class TweetsEndpoint {

    @Autowired
    private TweetsService tweetsService;

    @GetMapping(value = "/hashtags/count")
    public ResponseEntity<List<CountHashtags>> buscarContagemHashtags() {
        return ResponseEntity.ok(tweetsService.buscarContagemHashtags()) ;
    }

    @GetMapping(value = "/porhora/count")
    public ResponseEntity<List<CountTweets>> buscarContagemTweetsPorHora() {
        return ResponseEntity.ok(tweetsService.buscarContagemTweetsPorHora());
    }

    @GetMapping(value = "/top5")
    public ResponseEntity<List<Top5FollowersTweets>> buscarTop5Usuarios() {
        return ResponseEntity.ok(tweetsService.buscarTop5Usuarios());
    }

}
