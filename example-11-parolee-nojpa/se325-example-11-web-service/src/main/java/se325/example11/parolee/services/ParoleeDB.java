package se325.example11.parolee.services;

import se325.example11.parolee.domain.Parolee;

import java.util.*;

public class ParoleeDB {

    private long nextId = 1;

    private final Map<Long, Parolee> parolees = new TreeMap<>();

    public synchronized int size() {
        return parolees.size();
    }

    public synchronized long addParolee(Parolee p) {
        p.setId(nextId);
        nextId++;
        parolees.put(p.getId(), p);
        return p.getId();
    }

    public synchronized Parolee getParolee(long id) {
        return parolees.get(id);
    }

    public synchronized void removeParolee(long id) {
        parolees.remove(id);
    }

    public synchronized List<Parolee> getParolees() {
        return Collections.unmodifiableList(new ArrayList<>(parolees.values()));
    }

    public synchronized List<Parolee> getParolees(int start, int len) {
        List<Parolee> all = getParolees();
        return all.subList(start, Math.min(len + start, all.size()));
    }

    public synchronized void reset() {
        parolees.clear();
        nextId = 1;
    }
}
