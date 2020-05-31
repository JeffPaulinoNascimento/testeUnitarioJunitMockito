package br.ce.wcaquino.servicos;

import br.ce.wcaquino.dao.LocacaoDAO;
import br.ce.wcaquino.entidades.Filme;
import br.ce.wcaquino.entidades.Locacao;
import br.ce.wcaquino.entidades.Usuario;
import br.ce.wcaquino.exceptions.FilmeSemEstoqueException;
import br.ce.wcaquino.exceptions.LocadoraException;
import br.ce.wcaquino.utils.DataUtils;
import org.hamcrest.CoreMatchers;
import org.junit.*;
import org.junit.rules.ExpectedException;
import org.mockito.Mockito;

import java.util.*;

public class LocacaoServiceTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    private LocacaoService locacaoService;

    private SPCService spc;
    private LocacaoDAO dao;
    private EmailService emailService;

    // o junit reinicia todas as variaves a cada teste executado, para resolver o problema do
    // contador nao incrementando, coloque ele como static, dessa forma ela deixa de estar dentro do escopo do teste
    // e passa a estar no escopo da classe
//    private int contador = 0;

    //executa antes de cada metodo de test
    @Before
    public void setup(){
        System.out.println("antes");
        locacaoService = new LocacaoService();

        dao = Mockito.mock(LocacaoDAO.class);
        spc = Mockito.mock(SPCService.class);
        emailService = Mockito.mock(EmailService.class);

        locacaoService.setLocacaoDAO(dao);
        locacaoService.setSpcService(spc);
        locacaoService.setEmailService(emailService);

//        this.contador ++;
//        System.out.println("contador: " + contador);
    }

    //executa depois de cada metodo de test
    @After
    public void tearDown(){
        System.out.println("depois");

    }

//    //executa antes de cada instancia
//    @BeforeClass
//    public static void setupClass(){
//        System.out.println("antes de cada instancia");
//    }
//
//    //executa depois de cada metodo de test
//    @AfterClass
//    public static void tearDownClass(){
//        System.out.println("depois da instancia ser destruida");
//    }

    @Test
    @Ignore
    public void deveAlugarFilme() throws FilmeSemEstoqueException, LocadoraException {

        Assume.assumeFalse(DataUtils.verificarDiaSemana(new Date(), Calendar.SATURDAY) );

        Usuario usuario = new Usuario("Usuario 1");
        Filme filme = new Filme("Filme 1", 2, 5.0);

        Locacao locacao = locacaoService.alugarFilme(usuario, Collections.singletonList(filme));

        Assert.assertEquals(5.0, locacao.getValor(), 0.01);
        Assert.assertThat(locacao.getValor(), CoreMatchers.is(5.0));
        Assert.assertThat(locacao.getValor(), CoreMatchers.is(CoreMatchers.equalTo(5.0)) );
        Assert.assertThat(locacao.getValor(), CoreMatchers.is(CoreMatchers.not(4.0)));
        Assert.assertTrue(DataUtils.isMesmaData(locacao.getDataLocacao(), new Date()));
        Assert.assertTrue(DataUtils.isMesmaData(locacao.getDataRetorno(), DataUtils.obterDataComDiferencaDias(1)));

    }

    /*** Tratamento de excessões ***/

    @Test(expected = FilmeSemEstoqueException.class)
    public void deveLancarExcecaoAoAlugarFilmeSemEstoque() throws Exception {

        Usuario usuario = new Usuario("Usuario 1");
        Filme filme = new Filme("Filme 1", 0, 5.0);

        Locacao locacao = locacaoService.alugarFilme(usuario, Collections.singletonList(filme));
    }

    @Test
    public void naoDeveAlugarFilmeSemUsuario() throws FilmeSemEstoqueException {

        Filme filme = new Filme("Filme 1", 2, 5.0);

        try {
            locacaoService.alugarFilme(null, Collections.singletonList(filme));
            Assert.fail();

        } catch (LocadoraException e) {
            Assert.assertThat(e.getMessage(), CoreMatchers.is("Usuario vazio"));
        }

    }

    @Test
    public void naoDeveAlugarFilmeSemFilme() throws FilmeSemEstoqueException, LocadoraException {

        Usuario usuario = new Usuario("Usuario 1");

        expectedException.expect(LocadoraException.class);
        expectedException.expectMessage("Filme vazio");

        locacaoService.alugarFilme(usuario, null);

    }

    /*** Fim Tratamento de excessões ***/

    @Test
    public void devePagar75PorCentoNoFilme3() throws FilmeSemEstoqueException, LocadoraException {
        Usuario usuario = new Usuario("Usuario 1");

        List<Filme> filmes = Arrays.asList(
                new Filme("Filme 1 ", 2, 4.0),
                new Filme("Filme 2 ", 3, 4.0),
                new Filme("Filme 3 ", 5, 4.0) );

        Locacao resultado = locacaoService.alugarFilme(usuario, filmes);

        Assert.assertThat(resultado.getValor(), CoreMatchers.is(11.0));
    }

    @Test
    public void devePagar50PorCentoNoFilme4() throws FilmeSemEstoqueException, LocadoraException {
        Usuario usuario = new Usuario("Usuario 1");

        List<Filme> filmes = Arrays.asList(
                new Filme("Filme 1 ", 2, 4.0),
                new Filme("Filme 2 ", 3, 4.0),
                new Filme("Filme 3 ", 5, 4.0),
                new Filme("Filme 4 ", 3, 4.0) );

        Locacao resultado = locacaoService.alugarFilme(usuario, filmes);

        Assert.assertThat(resultado.getValor(), CoreMatchers.is(13.0));
    }

    @Test
    public void deveDevolverNaSegundaAoAlugarNoSabado() throws FilmeSemEstoqueException, LocadoraException {

        Assume.assumeTrue(DataUtils.verificarDiaSemana(new Date(), Calendar.SATURDAY));

        Usuario usuario = new Usuario("Usuario 1");

        List<Filme> filmes = Arrays.asList(
                new Filme("Filme 1 ", 2, 4.0) );

        Locacao retorno = locacaoService.alugarFilme(usuario, filmes);

        boolean isSegunda = DataUtils.verificarDiaSemana(retorno.getDataRetorno(), Calendar.MONDAY);

        Assert.assertTrue(isSegunda);

    }

    @Test
    public void naoDeveAlugarFilmeParaNegativadoSPC() throws FilmeSemEstoqueException, LocadoraException {
        Usuario usuario = new Usuario("Usuario 1");

        List<Filme> filmes = Arrays.asList(
                new Filme("Filme 1 ", 2, 4.0) );

        Mockito.when(spc.possuiNegativacao(usuario)).thenReturn(true);

        expectedException.expect(LocadoraException.class);
        expectedException.expectMessage("Usuario negativado");

        locacaoService.alugarFilme(usuario, filmes);
    }

    @Test
    public void deveEnviarEmailParaLocacoesAtrasadas() {
        Usuario usuario = new Usuario("Usuario 1");

        Locacao locacao = new Locacao();
        locacao.setDataLocacao(new Date());
        locacao.setFilmes(Arrays.asList( new Filme("Filme 1 ", 2, 4.0)) );
        locacao.setUsuario(usuario);
        locacao.setValor(5.0);
        locacao.setDataRetorno(DataUtils.obterDataComDiferencaDias(-2));

        List<Locacao> locacoes = Arrays.asList(locacao);

        Mockito.when(dao.obterLocacoesPendentes()).thenReturn(locacoes);

        locacaoService.notificarAtrasos();

        Mockito.verify(emailService).notifcarAtraso(usuario);
    }

}
