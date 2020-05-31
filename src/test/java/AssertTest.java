import br.ce.wcaquino.entidades.Usuario;
import org.junit.Assert;
import org.junit.Test;

public class AssertTest {

    @Test
    public void test() {
        Assert.assertTrue(true);
        Assert.assertFalse(false);

        Assert.assertEquals(1, 1);

        //precisa colocar o delta que limita até qual casa decimal ele vai comparar, nesse caso 0.512
        Assert.assertEquals(0.51235788559, 0.5124789, 0.001);
        Assert.assertEquals(Math.PI, 3.14, 0.01);
        Assert.assertEquals("bola", "bola");

        Assert.assertNotEquals("bola", "casa");

        Assert.assertTrue("bola".equalsIgnoreCase("Bola"));

        Usuario usuario1 = new Usuario("Usuario");
        Usuario usuario2 = new Usuario("Usuario");
        Usuario usuario3 = null;

        // Foi implementado o equals na classe Usuario
        Assert.assertEquals(usuario1, usuario2);

        //verifica se os objetos estão na mesma instancia
        Assert.assertSame(usuario2, usuario2);

        Assert.assertNull(usuario3);
        Assert.assertNotNull(usuario2);
    }
}
