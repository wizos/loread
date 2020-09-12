package me.wizos.loread.gson;

public interface GsonEnum<E> {
    String serialize();
    E deserialize(String jsonEnum);
}