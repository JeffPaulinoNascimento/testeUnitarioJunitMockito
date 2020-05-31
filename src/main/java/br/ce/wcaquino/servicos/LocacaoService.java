package br.ce.wcaquino.servicos;

import static br.ce.wcaquino.utils.DataUtils.adicionarDias;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import br.ce.wcaquino.dao.LocacaoDAO;
import br.ce.wcaquino.entidades.Filme;
import br.ce.wcaquino.entidades.Locacao;
import br.ce.wcaquino.entidades.Usuario;
import br.ce.wcaquino.exceptions.FilmeSemEstoqueException;
import br.ce.wcaquino.exceptions.LocadoraException;
import br.ce.wcaquino.utils.DataUtils;
import org.junit.Assert;
import org.junit.Test;
import sun.text.resources.iw.FormatData_iw_IL;

public class LocacaoService {

	private LocacaoDAO locacaoDAO;
	private SPCService spcService;
	private EmailService emailService;
	
	public Locacao alugarFilme(Usuario usuario, List<Filme> filmes) throws FilmeSemEstoqueException, LocadoraException {

		if (usuario == null)
			throw new LocadoraException("Usuario vazio");

		if (filmes == null || filmes.isEmpty())
			throw new LocadoraException("Filme vazio");

		for (Filme filme : filmes) {
			if (filme.getEstoque() == 0)
				throw new FilmeSemEstoqueException();
		}

		if (spcService.possuiNegativacao(usuario))
			throw new LocadoraException("Usuario negativado");


		Locacao locacao = new Locacao();
		locacao.setFilmes(filmes);
		locacao.setUsuario(usuario);
		locacao.setDataLocacao(new Date());
		Double valorTotal = 0d;

		for (int i = 0; i < filmes.size(); i++) {
			Filme filme = filmes.get(i);
			Double valorFilme = filme.getPrecoLocacao();

			if (i == 2)
				valorFilme = valorFilme * 0.75;

			if (i == 3)
				valorFilme = valorFilme * 0.5;

			valorTotal += valorFilme;

		}

		locacao.setValor(valorTotal);

		//Entrega no dia seguinte
		Date dataEntrega = new Date();
		dataEntrega = adicionarDias(dataEntrega, 1);
		if (DataUtils.verificarDiaSemana(dataEntrega, Calendar.SUNDAY))
			dataEntrega = adicionarDias(dataEntrega, 1);

		locacao.setDataRetorno(dataEntrega);
		
		//Salvando a locacao...	
		locacaoDAO.salvar(locacao);
		
		return locacao;
	}

	public void notificarAtrasos(){
		List<Locacao> locacoes = locacaoDAO.obterLocacoesPendentes();

		for (Locacao locacao : locacoes) {
			emailService.notifcarAtraso(locacao.getUsuario());

		}
	}

	public void setLocacaoDAO(LocacaoDAO dao){
		this.locacaoDAO = dao;
	}

	public void setSpcService(SPCService spc){
		this.spcService = spc;
	}

	public void setEmailService(EmailService emailService) {
		this.emailService = emailService;
	}
}