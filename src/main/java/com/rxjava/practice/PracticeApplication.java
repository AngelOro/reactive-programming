package com.rxjava.practice;

import com.rxjava.practice.model.Comentarios;
import com.rxjava.practice.model.Usuario;
import com.rxjava.practice.model.UsuarioComentarios;
import io.reactivex.rxjava3.core.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@SpringBootApplication
public class PracticeApplication implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(PracticeApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(PracticeApplication.class, args);
    }

    /* LOS FLUJOS SON INMUTABLES  */

    @Override
    public void run(String... args) throws Exception {
		System.out.println("----------------------------- EXAMPLE 1 ------------------------------");
		EjemploIterable();
		System.out.println("----------------------------- EXAMPLE 2 ------------------------------");
		EjemploIterableFlatMap();
		System.out.println("----------------------------- EXAMPLE 3 ------------------------------");
		EjemploConvertirString();
		System.out.println("----------------------------- EXAMPLE 4 ------------------------------");
		EjemploConvertirCollectList();
		System.out.println("----------------------------- EXAMPLE 5 ------------------------------");
		ejemploUsuarioComentarioFlatMap();
		System.out.println("----------------------------- EXAMPLE 6 ------------------------------");
		ejemploUsuarioComentarioZipWith();
		System.out.println("----------------------------- EXAMPLE 7 ------------------------------");
		ejemploUsuarioComentarioZipWith2();
		System.out.println("----------------------------- EXAMPLE 7 ------------------------------");
		ejemploZipWithRangos();

	}

	private void EjemploIterable() {
		//Flux<Usuario> nombres = Flux.just("Angelica Orozco", "Caro Osorio", "Jimena Martinez", "Maria Lopera", "Alvaro Ruiz", "Maria Mesa")

		List<String> userData = new ArrayList<>();
		userData.add("Angelica Orozco");
		userData.add("Caro Osorio");
		userData.add("Jimena Martinez");
		userData.add("Maria Lopera");
		userData.add("Alvaro Ruiz");
		userData.add( "Maria Mesa");

		Flux<String> nombres = Flux.fromIterable(userData); // Flujo de Strings reactivo

		Flux<Usuario> usuarios = nombres.map(nombre -> new Usuario(nombre.split(" ")[0].toUpperCase(), nombre.split(" ")[1].toUpperCase()))
				.filter(usuario -> usuario.getNombre().equals("MARIA"))
                .doOnNext(s -> {
					if (s == null) {
                        throw new RuntimeException("Nombre no pueden ser vacios");
                    }
                    System.out.println(s.getNombre().concat(" ").concat(s.getApellido()));
                })
                .map(user -> {
                    String nombre = user.getNombre().toLowerCase();
                    user.setNombre(nombre);
                    return user;
                });

		usuarios.subscribe(e -> log.info(e.toString()),
				error -> log.error(error.getMessage()),
				() -> log.info("Ha finalizado la ejecución correctamente"));
	}
	private void EjemploIterableFlatMap() {
		//Flux<Usuario> nombres = Flux.just("Angelica Orozco", "Caro Osorio", "Jimena Martinez", "Maria Lopera", "Alvaro Ruiz", "Maria Mesa")

		List<String> userData = new ArrayList<>();
		userData.add("Angelica Orozco");
		userData.add("Caro Osorio");
		userData.add("Jimena Martinez");
		userData.add("Maria Lopera");
		userData.add("Alvaro Ruiz");
		userData.add( "Maria Mesa");

		Flux.fromIterable(userData)
				.map(nombre -> new Usuario(nombre.split(" ")[0].toUpperCase(), nombre.split(" ")[1].toUpperCase()))
				.flatMap(usuario -> {
					if(usuario.getNombre().equalsIgnoreCase("ANGELICA")){
						return Mono.just(usuario); // Modficamos el flujo original mediante un nuevo observable, fucionando al mismo string
					}else{
						return Mono.empty();
					}
				})
				.map(user -> {
					String nombre = user.getNombre().toLowerCase();
					user.setNombre(nombre);
					return user;
				})
				.subscribe(e -> log.info(e.toString()));
	}
	private void EjemploConvertirString() {
		//Flux<Usuario> nombres = Flux.just("Angelica Orozco", "Caro Osorio", "Jimena Martinez", "Maria Lopera", "Alvaro Ruiz", "Maria Mesa")

		List<Usuario> userData = new ArrayList<>();
		userData.add(new Usuario("Angelica", "Orozco"));
		userData.add(new Usuario("Caro", "Osorio"));
		userData.add(new Usuario("Jimena", "Martinez"));
		userData.add(new Usuario("Maria", "Lopera"));
		userData.add(new Usuario("Alvaro", "Ruiz"));
		userData.add(new Usuario("Maria", "Mesa"));

		Flux.fromIterable(userData)
				.map(usuario -> usuario.getNombre().toUpperCase().concat(" ").concat(usuario.getApellido()).toUpperCase())
				.flatMap(nombre -> {
					if(nombre.contains("ANGELICA")){
						return Mono.just(nombre); // Modficamos el flujo original mediante un nuevo observable, fucionando al mismo string
					}else{
						return Mono.empty();
					}
				})
				.map(String::toLowerCase)
				.subscribe(e -> log.info(e.toString()));
	}

	private void EjemploConvertirCollectList() {
		//Flux<Usuario> nombres = Flux.just("Angelica Orozco", "Caro Osorio", "Jimena Martinez", "Maria Lopera", "Alvaro Ruiz", "Maria Mesa")

		List<Usuario> userData = new ArrayList<>();
		userData.add(new Usuario("Angelica", "Orozco"));
		userData.add(new Usuario("Caro", "Osorio"));
		userData.add(new Usuario("Jimena", "Martinez"));
		userData.add(new Usuario("Maria", "Lopera"));
		userData.add(new Usuario("Alvaro", "Ruiz"));
		userData.add(new Usuario("Maria", "Mesa"));
/*
		Flux.fromIterable(userData)
				.collectList() //Convertir a un mono que contiene un solo objeto, sin el collect nos muestra todos los usuarios divididos
				.subscribe(usuario -> log.info(usuario.toString()));*/

		//Acá se emite una sola vez y se recorre

		Flux.fromIterable(userData)
				.collectList() //Convertir a un mono que contiene un solo objeto, sin el collect nos muestra todos los usuarios divididos
				.subscribe(usuario -> {
					usuario.forEach(item -> log.info(item.toString()));
				});
	}

	/*
	* Se tiene dos flujos distintos (Usuario y Comentarios), se combinan ambos en uno solo
	* */

	private void ejemploUsuarioComentarioFlatMap(){
		Mono<Usuario> usuarioMono = Mono.fromCallable(() -> new Usuario("Hello", "KKK"));
		Mono<Comentarios> comentariosMono = Mono.fromCallable(() -> {
			Comentarios comentarios = new Comentarios();
			comentarios.addComentarios("Hola a todos! ");
			comentarios.addComentarios("Help Me! ");
			comentarios.addComentarios("Yo soy capaz gonorreas!! ");
			return comentarios;
		});

		/*
		* A partir de usuarioMono convertir a UsuarioComentarios
		* convertimos el observable comentariosMono en el tipo usuarioComentarios mediante el map
		* */
		usuarioMono.flatMap(usuario -> comentariosMono.map(comentarios -> new UsuarioComentarios(usuario, comentarios)))
		.subscribe(usuarioComentarios -> log.info(usuarioComentarios.toString()));
	}

	//ZipWith toma dos flujos y los combina

	private void ejemploUsuarioComentarioZipWith(){
		Mono<Usuario> usuarioMono = Mono.fromCallable(() -> new Usuario("Hello", "KKK"));
		Mono<Comentarios> comentariosMono = Mono.fromCallable(() -> {
			Comentarios comentarios = new Comentarios();
			comentarios.addComentarios("Hola a ZipWith! ");
			comentarios.addComentarios("Help Me! ");
			comentarios.addComentarios("Yo soy capaz gonorreas!! ");
			return comentarios;
		});

		usuarioMono.zipWith(comentariosMono, UsuarioComentarios::new)
				.subscribe(usuarioComentarios -> log.info(usuarioComentarios.toString()));
	}

	private void ejemploUsuarioComentarioZipWith2(){
		Mono<Usuario> usuarioMono = Mono.fromCallable(() -> new Usuario("Hello", "KKK"));
		Mono<Comentarios> comentariosMono = Mono.fromCallable(() -> {
			Comentarios comentarios = new Comentarios();
			comentarios.addComentarios("Hola a ZipWith Two! ");
			comentarios.addComentarios("Help Me! ");
			comentarios.addComentarios("Yo soy capaz gonorreas!! ");
			return comentarios;
		});

		usuarioMono.zipWith(comentariosMono)
				.map(objects -> {
					Usuario usuario = objects.getT1();
					Comentarios comentarios = objects.getT2();
					return new UsuarioComentarios(usuario, comentarios);
				}).subscribe(usuarioComentarios -> log.info(usuarioComentarios.toString()));
	}

	private void ejemploZipWithRangos(){
		Flux.just(1,2,3,4)
				.map(integer -> (integer * 2))
				.zipWith(Flux.range(0, 4), (uno, dos) -> String.format("Primer Flux: %d, Segundo Flux: %d", uno, dos))
				.subscribe(s -> log.info(s));
	}

	/*
	* Intervalos de tiempo
	* */

}
