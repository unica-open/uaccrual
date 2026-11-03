/*
*SPDX-FileCopyrightText: Copyright 2020 | CSI Piemonte
*SPDX-License-Identifier: EUPL-1.2
*/
--Data modifica 22/05/2026


-- l'univocità del record è dato da codice e gest_data_ins ma con una data precisa al secondo come si puo' evitare un inserimento doppio ? 
-- per questo ho usato la condizione sulla data_del no null.
INSERT INTO accrual.accrual_d_file_stato (file_stato_codice, file_stato_descrizione, gest_data_ins, gest_utente_ins)
SELECT tmp.file_stato_codice, tmp.file_stato_descrizione, tmp.gest_data_ins,tmp.gest_utente_ins
FROM (VALUES
  ('IN_ATTESA_CONFERMA_UTENTE','In attesa conferma utente',now(),'admin')
) AS tmp(file_stato_codice, file_stato_descrizione, gest_data_ins, gest_utente_ins)
WHERE NOT EXISTS (
  SELECT *
  FROM accrual.accrual_d_file_stato current
  WHERE current.file_stato_codice = tmp.file_stato_codice
--  and current.gest_data_ins = tmp.gest_data_ins::timestamp
  and gest_data_del is null
);

ALTER TABLE IF EXISTS accrual.accrual_t_prima_nota drop constraint if exists idx_accrual_t_prima_nota_1;
ALTER TABLE if exists accrual.accrual_t_prima_nota ADD constraint  idx_accrual_t_prima_nota_1 UNIQUE (prima_nota_numero, prima_nota_anno, ente_id);


insert into accrual_r_ruolo_azione (ruolo_codice,azione_id,ente_id,inizio_validita,gest_utente_ins)

/* SPECIFICO PER ENTI CSI COMPRESO ENTE TEST: se si vuole applicare la configurazioni ad altri enti occorre inserire il codice_univoco dell'ente che interessa - INIZIO */
select tmp.ruolo_codice,a.azione_id,e.ente_id,tmp.inizio_validita,tmp.gest_utente_ins
from (values
--consip,regp
	('SCRITTURA_LIMITATA','OP-GEN-updModMF','80087670016',now(),'admin'),
	('LETTURA','OP-GEN-updModMF','80087670016',now(),'admin'),
--cmto	
	('SCRITTURA_LIMITATA','OP-GEN-updModMF','01907990012',now(),'admin'),
	('LETTURA','OP-GEN-updModMF','01907990012',now(),'admin'),
--forn2,mult
	('SCRITTURA_LIMITATA','OP-GEN-updModMF','92116650349',now(),'admin'),
	('LETTURA','OP-GEN-updModMF','92116650349',now(),'admin'),
	('SCRITTURA_LIMITATA','OP-GEN-updModMF','97603810017',now(),'admin'),
	('LETTURA','OP-GEN-updModMF','97603810017',now(),'admin'),
	('SCRITTURA_LIMITATA','OP-GEN-updModMF','ENTE_TEST',now(),'admin'),
	('LETTURA','OP-GEN-updModMF','ENTE_TEST',now(),'admin'),
	('SCRITTURA_LIMITATA','OP-GEN-updModMF','97694170016',now(),'admin'),
	('LETTURA','OP-GEN-updModMF','97694170016',now(),'admin'),
	('SCRITTURA_LIMITATA','OP-GEN-updModMF','01550320061',now(),'admin'),
	('LETTURA','OP-GEN-updModMF','01550320061',now(),'admin')	
) as tmp (ruolo_codice,azione_codice,ente_codice_univoco,inizio_validita,gest_utente_ins)
join accrual_t_ente e on e.ente_codice_univoco=tmp.ente_codice_univoco and e.gest_data_del is null
join accrual_t_azione a on a.azione_codice=tmp.azione_codice and a.gest_data_del is null
where not exists (select 1
	from accrual_r_ruolo_azione current
	where current.ente_id = e.ente_id
	and current.ruolo_codice = tmp.ruolo_codice
	and current.azione_id = a.azione_id
	and current.gest_data_del is null);

/* SPECIFICO PER ENTI CSI COMPRESO ENTE TEST: se si vuole applicare la configurazioni ad altri enti occorre inserire il codice_univoco dell'ente che interessa - FINE */


ALTER TABLE accrual_t_ente ADD COLUMN IF NOT EXISTS ente_denominazione VARCHAR(500);
 
ALTER SEQUENCE accrual.accrual_r_prima_nota_stato_prima_nota_stato_r_id_seq INCREMENT BY 50;
ALTER SEQUENCE accrual.accrual_r_pdc_saldo_prima_nota_pdc_saldo_prima_nota_r_id_seq INCREMENT BY 50;
ALTER SEQUENCE accrual.accrual_r_etichetta_prima_nota_etichetta_prima_nota_r_id_seq INCREMENT BY 50;
ALTER SEQUENCE accrual.accrual_r_prima_nota_attributo_prima_nota_attributo_r_id_seq INCREMENT BY 50;
ALTER SEQUENCE accrual.accrual_t_etichetta_etichetta_id_seq INCREMENT BY 50;
ALTER SEQUENCE accrual.accrual_t_pdc_saldo_pdc_saldo_id_seq INCREMENT BY 50;
ALTER SEQUENCE accrual.accrual_t_prima_nota_importo_prima_nota_importo_id_seq INCREMENT BY 50;
ALTER SEQUENCE accrual.accrual_t_prima_nota_prima_nota_id_seq INCREMENT BY 50;
  
  
DROP FUNCTION if exists accrual.accrual_rep001_stampa_mastrino(p_ente_id integer, p_anno_bilancio varchar, p_data_da date, p_data_a date, p_conto_pdc varchar);
DROP FUNCTION if exists accrual.accrual_rep002_stampa_libro_giornale(p_ente_id integer, p_anno_bilancio varchar, p_code_tipo varchar, p_code_fonte varchar, p_code_evento varchar, p_code_causale varchar, p_numero_provvisorio integer, p_numero_definitivo integer, p_code_stato varchar, p_conto_pdc varchar, p_data_da_provv date, p_data_a_provv date, p_data_da_def date, p_data_a_def date);
DROP FUNCTION if exists accrual.accrual_rep003_stampa_bilancio_verifica(p_ente_id integer, p_anno_bilancio varchar, p_tipo_stampa varchar, p_stato_pn varchar);
DROP FUNCTION if exists accrual.accrual_rep003_stampa_bilancio_verifica(p_ente_id integer, p_anno_bilancio varchar, p_tipo_stampa varchar, p_stato_pn varchar, p_stampa_seg_utente varchar);

CREATE OR REPLACE FUNCTION accrual.accrual_rep001_stampa_mastrino (
  p_ente_id integer,
  p_anno_bilancio varchar,
  p_data_da date,
  p_data_a date,
  p_conto_pdc varchar
)
RETURNS TABLE (
  pdc_id1 integer,
  codice_pdc1 varchar,
  descr_pdc1 varchar,
  pdc_id2 integer,
  codice_pdc2 varchar,
  descr_pdc2 varchar,
  pdc_id3 integer,
  codice_pdc3 varchar,
  descr_pdc3 varchar,
  pdc_id4 integer,
  codice_pdc4 varchar,
  descr_pdc4 varchar,
  pdc_id5 integer,
  codice_pdc5 varchar,
  descr_pdc5 varchar,
  pdc_id6 integer,
  codice_pdc6 varchar,
  descr_pdc6 varchar,
  pdc_id7 integer,
  codice_pdc7 varchar,
  descr_pdc7 varchar,
  pdc_id8 integer,
  codice_pdc8 varchar,
  descr_pdc8 varchar,
  pdc_id9 integer,
  codice_pdc9 varchar,
  descr_pdc9 varchar,
  pdc_id10 integer,
  codice_pdc10 varchar,
  descr_pdc10 varchar,
  livello integer,
  prima_nota_id integer,
  prima_nota_numero integer,
  prima_nota_data date,
  prima_nota_progressivogiornale integer,
  prima_nota_dataregistrazionegiornale date,
  fonte_codice varchar,
  fonte_descrizione varchar,
  evento_codice varchar,
  evento_descrizione varchar,
  causale_codice varchar,
  causale_descrizione varchar,
  prima_nota_stato_codice varchar,
  prima_nota_stato_descrizione varchar,
  codice_tipo_prima_nota varchar,
  desc_tipo_prima_nota varchar,
  elenco_attributi varchar,
  importo_dare numeric,
  importo_avere numeric,
  importo_ini_dare numeric,
  importo_ini_avere numeric,
  display_error varchar
) LANGUAGE 'plpgsql'
VOLATILE
CALLED ON NULL INPUT
SECURITY INVOKER
PARALLEL UNSAFE
COST 100 ROWS 1000
AS
$body$
DECLARE
pdce_conto_id_in integer;
conta_prime_note integer;
cod_pdc_classe varchar;
cod_pdc_causale varchar;
importo_ini_dare numeric;
importo_ini_avere numeric;
parametri_causali varchar[];

BEGIN

/*
	26/02/2026.
    Versione 1 della procedura che estrae i dati del mastrino per ACCRUAL.
    
    Parametri in input:
    - p_ente_id; OBBLIGATORIO; id dell'ente;
    - p_anno_bilancio; OBBLIGATORIO; anno del bilancio;
    - p_data_da; OBBLIGATORIO; data a partire dalla quale cercare la prima nota;
    - p_data_a; OBBLIGATORIO; data fino alla quale cercare la prima nota;
    - p_conto_pdc, OBBLIGATORIO; codice del conto da cercare.
    
*/

--cerco l'id del conto in input e anche la sua famiglia.
select pdc.pdc_id, pdc_classe.pdc_classe_codice
	into pdce_conto_id_in, cod_pdc_classe
from accrual_t_pdc pdc, accrual_d_pdc_classe pdc_classe
where pdc_classe.pdc_classe_id=pdc.pdc_classe_id
and pdc.ente_id = p_ente_id
and pdc.pdc_codice_ufficiale = p_conto_pdc
and pdc.fine_validita IS NULL
and pdc.gest_data_del IS NULL
and pdc_classe.fine_validita IS NULL
and pdc_classe.gest_data_del IS NULL;

--se non esiste restituisco un errore al report.
IF NOT FOUND THEN
    display_error='Il codice PDC indicato ('||p_conto_pdc||') e'' inesistente';
    return next;
    return;
END IF;
    
raise notice 'pdce_conto_id_in di % = % - codice classe = %', p_conto_pdc,pdce_conto_id_in, cod_pdc_classe;

conta_prime_note:=0;

--verifico se esistono prime note definitive per il conto indicato nel periodo sepcificato
--per segnalare l'eventuale mancanza prima di effettuare la query vera e propria.
select count(*)
into conta_prime_note  
from accrual_t_prima_nota_importo imp,
	accrual_t_prima_nota pn,
    accrual_r_prima_nota_stato rstato, 
    accrual_d_prima_nota_stato dstato
where pn.prima_nota_id = imp.prima_nota_id
  and rstato.prima_nota_id= pn.prima_nota_id
  and rstato.prima_nota_stato_id=dstato.prima_nota_stato_id
  and imp.ente_id = p_ente_id
  and imp.pdc_id = pdce_conto_id_in
  and pn.prima_nota_anno = p_anno_bilancio::integer
  and dstato.prima_nota_stato_codice = 'D' --solo le prime note definitive.
  and date_trunc('day',pn.prima_nota_dataregistrazionegiornale) 
    	between p_data_da and p_data_a
  and imp.gest_data_del is null
  and pn.gest_data_del is null
  and rstato.gest_data_del is null
  and rstato.fine_validita is null;
  
if conta_prime_note = 0 then
	display_error='Non ci sono prime note per il conto '||p_conto_pdc|| ' nel periodo indicato';
    return next;
    return;
END IF;

/*
Per importo INIZIALE date/avere, da Donatella:

Andrea dice che il saldo iniziale dovrebbe rappresentare la prima nota di Apertura Patrimoniale 
nell'anno, che in siac non si riusciva a estrarre però mi sembra strano, hai verificato che in 
siac c'era 0 schiantato?

1) classe AP-attivo patrimoniale: per il Dare/Avere del saldo iniziale devo cercare la prima nota 
definitiva con causale APE_ATT
 
2) classe PP-Passivo Patrimoniale: per il Dare/Avere del saldo iniziale devo cercare la prima nota 
definitiva con causale APE_PASS
 
3) classe CE-Costi E Oneri D'Esercizio: per il Dare/Avere del saldo iniziale devo cercare la prima 
nota definitiva con causale APE_CE
 
4) classe RE-Ricavi E Proventi D'Esercizio: per il Dare/Avere del saldo iniziale devo cercare la 
prima nota definitiva con causale APE_RE
 
*/

--in base alla classe del conto in input imposto la relativa causale per la ricerca della 
--prima nota di Apertura Patrimoniale che serve per avere l'importo iniziale dare/avere.
--La causale e' configurata sulla tabella accrual_t_parametro con il parametro CAUSALI_APERTURA_118.
--I valori sono in un ordine preciso:
--1 APE_ATT per classe AP
--2 APE_PASS per classe PP
--3 APE_CE per classe CE
--4 APE_RE per classe RE.
select string_to_array(parametro_valore,',')
	into parametri_causali
from accrual_t_parametro a
where a.ente_id=p_ente_id
and a.parametro_nome='CAUSALI_APERTURA_118'
and a.gest_data_del IS NULL;

IF NOT FOUND THEN
    display_error='Per questo ente non è stato configuarato il parametro CAUSALI_APERTURA_118';
    return next;
    return;
END IF;

--raise notice 'valore 1 = %', parametri_causali[1];
--raise notice 'valore 2 = %', parametri_causali[2];
--raise notice 'valore 3 = %', parametri_causali[3];
--raise notice 'valore 4 = %', parametri_causali[4];

if cod_pdc_classe = 'AP' then
	cod_pdc_causale = parametri_causali[1];--'APE_ATT';
elsif cod_pdc_classe = 'PP' then
		cod_pdc_causale = parametri_causali[2];-- 'APE_PASS';
elsif cod_pdc_classe = 'CE' then
		cod_pdc_causale = parametri_causali[3];--'APE_CE'; 
elsif cod_pdc_classe = 'RE' then
		cod_pdc_causale = parametri_causali[4]; --'APE_RE';    
else cod_pdc_causale = '';
end if;     

raise notice 'Codice causale = %', cod_pdc_causale;

importo_ini_dare:=0;
importo_ini_avere:=0;

--cerco gli importi DARE/AVERE della prima nota DEFINITIVA della classe impostata prima
--legata al conto in input.
select COALESCE(sum(importi_dare.importi_dare),0), COALESCE(sum(importi_avere.importo_avere),0)
	into importo_ini_dare, importo_ini_avere
from accrual_t_causale cau,
	accrual_t_prima_nota pn
    	left join (select sum(pnimp.prima_nota_importo_importo) importo_avere, pnimp.prima_nota_id
                      from accrual_t_prima_nota_importo pnimp, accrual_t_pdc pdc
                      where pdc.pdc_id=pnimp.pdc_id
                          and pnimp.ente_id = p_ente_id
                          and pnimp.prima_nota_importo_segno='AVERE' 
                          and pdc.pdc_id = pdce_conto_id_in                         
                          and pnimp.gest_data_del is null
                          and pdc.gest_data_del is null
                      group by pnimp.prima_nota_id) importi_avere
        	on pn.prima_nota_id = importi_avere.prima_nota_id
        left join (select sum(pnimp.prima_nota_importo_importo) importi_dare, pnimp.prima_nota_id
                    from accrual_t_prima_nota_importo pnimp, accrual_t_pdc pdc
                    where pdc.pdc_id=pnimp.pdc_id
                          and pnimp.ente_id = p_ente_id
                          and pnimp.prima_nota_importo_segno='DARE' 
                          and pdc.pdc_id = pdce_conto_id_in  
                          and pnimp.gest_data_del is null
                          and pdc.gest_data_del is null
                    group by pnimp.prima_nota_id) importi_dare  
        	on pn.prima_nota_id = importi_dare.prima_nota_id,
    accrual_r_prima_nota_stato rstato, 
    accrual_d_prima_nota_stato dstato  
where cau.causale_id=pn.causale_id
    and rstato.prima_nota_id=pn.prima_nota_id
	and dstato.prima_nota_stato_id=rstato.prima_nota_stato_id
	and pn.ente_id=p_ente_id
    and pn.prima_nota_anno = p_anno_bilancio::integer
    and dstato.prima_nota_stato_codice = 'D'
    and cau.causale_codice = cod_pdc_causale
    and pn.gest_data_del is null
    and rstato.gest_data_del is null
    and rstato.fine_validita is null
	and dstato.gest_data_del is null;

raise notice 'importo_ini_dare = % - importo_ini_avere = %', importo_ini_dare, importo_ini_avere;

--estrazione dei dati.
return query 
--my_tree_unico serve per raggruppare in un'unica riga i dati dell'albero dei conti.
--Viene restituito anche l'id del conto in input (pdc_id_foglia) in modo che tutti i dati delle
--prime note di quel conto siano agganciati al record creato con my_tree_unico.
--In pratica per ogni prima nota avremo sullo stesso record i dati dell'albero dei conti e questo
--facilita la gestione della presentazione dei dati dell'albero nel report.
with my_tree_unico as materialized (
  WITH RECURSIVE my_tree AS materialized (
    -- Seed
    SELECT 
      a.pdc_id,
      a.pdc_id_padre,
      a.pdc_codice_ufficiale,
      a.pdc_codice,
      a.pdc_descrizione,
      a.pdc_livello,
      1 AS livellotree
    FROM accrual_t_pdc a
    WHERE a.ente_id = p_ente_id
    	and a.pdc_id = pdce_conto_id_in
        and a.gest_data_del is null
    UNION ALL
    -- Recursive Term
    SELECT 
      b.pdc_id,
      b.pdc_id_padre,
      b.pdc_codice_ufficiale,
      b.pdc_codice,
      b.pdc_descrizione,
      b.pdc_livello,
      t.livellotree + 1
    FROM my_tree t
    INNER JOIN accrual_t_pdc b
      ON t.pdc_id_padre = b.pdc_id
  )
  SELECT
    MAX(CASE WHEN pdc_livello = 1 THEN pdc_id END) AS pdc_id1,
    MAX(CASE WHEN pdc_livello = 1 THEN pdc_codice_ufficiale END) AS codice_pdc1,
    MAX(CASE WHEN pdc_livello = 1 THEN pdc_descrizione END) AS descr_pdc1,
    MAX(CASE WHEN pdc_livello = 2 THEN pdc_id END) AS pdc_id2,
    MAX(CASE WHEN pdc_livello = 2 THEN pdc_codice_ufficiale END) AS codice_pdc2,
    MAX(CASE WHEN pdc_livello = 2 THEN pdc_descrizione END) AS descr_pdc2,
    MAX(CASE WHEN pdc_livello = 3 THEN pdc_id END) AS pdc_id3,
    MAX(CASE WHEN pdc_livello = 3 THEN pdc_codice_ufficiale END) AS codice_pdc3,
    MAX(CASE WHEN pdc_livello = 3 THEN pdc_descrizione END) AS descr_pdc3,
    MAX(CASE WHEN pdc_livello = 4 THEN pdc_id END) AS pdc_id4,
    MAX(CASE WHEN pdc_livello = 4 THEN pdc_codice_ufficiale END) AS codice_pdc4,
    MAX(CASE WHEN pdc_livello = 4 THEN pdc_descrizione END) AS descr_pdc4,
    MAX(CASE WHEN pdc_livello = 5 THEN pdc_id END) AS pdc_id5,
    MAX(CASE WHEN pdc_livello = 5 THEN pdc_codice_ufficiale END) AS codice_pdc5,
    MAX(CASE WHEN pdc_livello = 5 THEN pdc_descrizione END) AS descr_pdc5,
    MAX(CASE WHEN pdc_livello = 6 THEN pdc_id END) AS pdc_id6,
    MAX(CASE WHEN pdc_livello = 6 THEN pdc_codice_ufficiale END) AS codice_pdc6,
    MAX(CASE WHEN pdc_livello = 6 THEN pdc_descrizione END) AS descr_pdc6,
    MAX(CASE WHEN pdc_livello = 7 THEN pdc_id END) AS pdc_id7,
    MAX(CASE WHEN pdc_livello = 7 THEN pdc_codice_ufficiale END) AS codice_pdc7,
    MAX(CASE WHEN pdc_livello = 7 THEN pdc_descrizione END) AS descr_pdc7,
    MAX(CASE WHEN pdc_livello = 8 THEN pdc_id END) AS pdc_id8,
    MAX(CASE WHEN pdc_livello = 8 THEN pdc_codice_ufficiale END) AS codice_pdc8,
    MAX(CASE WHEN pdc_livello = 8 THEN pdc_descrizione END) AS descr_pdc8,
    MAX(CASE WHEN pdc_livello = 9 THEN pdc_id END) AS pdc_id9,
    MAX(CASE WHEN pdc_livello = 9 THEN pdc_codice_ufficiale END) AS codice_pdc9,
    MAX(CASE WHEN pdc_livello = 9 THEN pdc_descrizione END) AS descr_pdc9,
    MAX(CASE WHEN pdc_livello = 10 THEN pdc_id END) AS pdc_id10,
    MAX(CASE WHEN pdc_livello = 10 THEN pdc_codice_ufficiale END) AS codice_pdc10,
    MAX(CASE WHEN pdc_livello = 10 THEN pdc_descrizione END) AS descr_pdc10,
    max(pdc_livello) livello,
    pdce_conto_id_in  pdc_id_foglia
  FROM my_tree),
prime_note as materialized (select pn.prima_nota_id, pn.prima_nota_numero, pn.prima_nota_data, pn.prima_nota_progressivogiornale,
  pn.prima_nota_dataregistrazionegiornale, fonte.fonte_codice, fonte.fonte_descrizione, 
  evento.evento_codice, evento.evento_descrizione, cau.causale_codice, cau.causale_descrizione,
  dstato.prima_nota_stato_codice, dstato.prima_nota_stato_descrizione, 
  tipopn.prima_nota_tipo_codice codice_tipo_prima_nota, 
  tipopn.prima_nota_tipo_descrizione desc_tipo_prima_nota,
  --gli attributi possono essere legati alla prima nota direttamente dalla tabella
  --accrual_r_prima_nota_attributo oppure tramite la tabella degli importi delle prima nota 
  --accrual_t_prima_nota_importo.
  --Per questo motivo devo fare una UNION.
  (select string_agg(attributo_descrizione||'-'||prima_nota_attributo_valore,', ' order by prima_nota_attributo_valore) 
 	from (select attr.attributo_descrizione,rattr.prima_nota_attributo_valore 
 		from accrual_d_attributo attr, 
            accrual_r_prima_nota_attributo rattr,
            accrual_t_prima_nota_importo imp
            left join accrual_t_pdc pdc 
                    on pdc.pdc_id = imp.pdc_id
          where attr.attributo_id=rattr.attributo_id
          and rattr.prima_nota_importo_id = imp.prima_nota_importo_id
          and rattr.prima_nota_id=pn.prima_nota_id 
          and rattr.ente_id = p_ente_id          
          and pdc.pdc_id = pdce_conto_id_in
          and rattr.gest_data_del is null
          and attr.gest_data_del is null
    union 
      select attr.attributo_descrizione,rattr.prima_nota_attributo_valore 
        from accrual_d_attributo attr, 
          accrual_r_prima_nota_attributo rattr,
          accrual_t_prima_nota_importo imp
          left join accrual_t_pdc pdc 
                  on pdc.pdc_id = imp.pdc_id
        where attr.attributo_id=rattr.attributo_id
            and imp.prima_nota_id=rattr.prima_nota_id
            and rattr.prima_nota_id=pn.prima_nota_id
            and rattr.ente_id = p_ente_id             
            and pdc.pdc_id = pdce_conto_id_in
            and rattr.gest_data_del is null
            and attr.gest_data_del is null
            and   rattr.prima_nota_importo_id IS NULL) tutto   ) elenco_attributi,
 pdce_conto_id_in pdc_id
from accrual_d_fonte fonte, accrual_t_evento evento, accrual_t_causale cau,
  accrual_t_prima_nota pn, accrual_d_prima_nota_tipo tipopn,
  accrual_r_prima_nota_stato rstato, accrual_d_prima_nota_stato dstato,
  accrual_r_causale_stato r_cau_stato, accrual_d_causale_stato d_cau_stato
where cau.evento_id=evento.evento_id
	and pn.causale_id=cau.causale_id
	and pn.fonte_id=fonte.fonte_id
	and pn.prima_nota_tipo_id=tipopn.prima_nota_tipo_id
	and rstato.prima_nota_id=pn.prima_nota_id
	and dstato.prima_nota_stato_id=rstato.prima_nota_stato_id
    and r_cau_stato.causale_id=cau.causale_id
    and r_cau_stato.causale_stato_id=d_cau_stato.causale_stato_id
    and pn.ente_id = p_ente_id
    and pn.prima_nota_anno = p_anno_bilancio::integer
    and dstato.prima_nota_stato_codice = 'D' --solo le prime note definitive.
    and date_trunc('day',pn.prima_nota_dataregistrazionegiornale) 
    	between p_data_da and p_data_a
        --devo escludere le prime note inziali, cioe' quelle che danno origine agli importi iniziali.
    and cau.causale_codice <> cod_pdc_causale
    and d_cau_stato.causale_stato_codice='V'
	and fonte.gest_data_del is null
	and evento.gest_data_del is null
	and cau.gest_data_del is null
	and pn.gest_data_del is null
	and tipopn.gest_data_del is null
	and rstato.gest_data_del is null
    and rstato.fine_validita is null
	and dstato.gest_data_del is null
    and r_cau_stato.fine_validita IS NULL
    and r_cau_stato.gest_data_del IS NULL
    	--verifico che sulle prime note esista almeno 1 importo valido per il conto in input.
    and exists (select 1  
    			from accrual_t_prima_nota_importo imp
                where imp.prima_nota_id=pn.prima_nota_id
                  and imp.ente_id = p_ente_id
                  and imp.pdc_id = pdce_conto_id_in 
                  and imp.gest_data_del is null)),
	importi_avere as materialized (    				  
    	select sum(pnimp.prima_nota_importo_importo) importo_avere, pnimp.prima_nota_id
        from accrual_t_prima_nota_importo pnimp, accrual_t_pdc pdc
        where pdc.pdc_id=pnimp.pdc_id
            and pnimp.ente_id = p_ente_id
            and pnimp.prima_nota_importo_segno='AVERE'
            and pdc.pdc_id = pdce_conto_id_in 
            and pnimp.gest_data_del is null
            and pdc.gest_data_del is null
        group by pdc.pdc_codice, pdc.pdc_id, pnimp.prima_nota_id),
    importi_dare as materialized (
    	select sum(pnimp.prima_nota_importo_importo) importo_dare, pnimp.prima_nota_id
        from accrual_t_prima_nota_importo pnimp, accrual_t_pdc pdc
        where pdc.pdc_id=pnimp.pdc_id
              and pnimp.ente_id = p_ente_id
              and pnimp.prima_nota_importo_segno='DARE'
              and pdc.pdc_id = pdce_conto_id_in 
              and pnimp.gest_data_del is null
              and pdc.gest_data_del is null
        group by pdc.pdc_codice, pdc.pdc_id, pnimp.prima_nota_id)    
select  COALESCE(my_tree_unico.pdc_id1,0)::integer pdc_id1,
	COALESCE(my_tree_unico.codice_pdc1,'')::varchar codice_pdc1,
    COALESCE(my_tree_unico.descr_pdc1,'')::varchar descr_pdc1,
	COALESCE(my_tree_unico.pdc_id2,0)::integer pdc_id2,
	COALESCE(my_tree_unico.codice_pdc2,'')::varchar codice_pdc2,
    COALESCE(my_tree_unico.descr_pdc2,'')::varchar descr_pdc2, 
    COALESCE(my_tree_unico.pdc_id3,0)::integer pdc_id3,
	COALESCE(my_tree_unico.codice_pdc3,'')::varchar codice_pdc3,
    COALESCE(my_tree_unico.descr_pdc3,'')::varchar descr_pdc3,   
    COALESCE(my_tree_unico.pdc_id4,0)::integer pdc_id1,
	COALESCE(my_tree_unico.codice_pdc4,'')::varchar codice_pdc1,
    COALESCE(my_tree_unico.descr_pdc4,'')::varchar descr_pdc4,
    COALESCE(my_tree_unico.pdc_id5,0)::integer pdc_id5,
	COALESCE(my_tree_unico.codice_pdc5,'')::varchar codice_pdc5,
    COALESCE(my_tree_unico.descr_pdc5,'')::varchar descr_pdc5,
    COALESCE(my_tree_unico.pdc_id6,0)::integer pdc_id6,
	COALESCE(my_tree_unico.codice_pdc6,'')::varchar codice_pdc6,
    COALESCE(my_tree_unico.descr_pdc6,'')::varchar descr_pdc6,
    COALESCE(my_tree_unico.pdc_id7,0)::integer pdc_id17,
	COALESCE(my_tree_unico.codice_pdc7,'')::varchar codice_pdc7,
    COALESCE(my_tree_unico.descr_pdc7,'')::varchar descr_pdc7,
    COALESCE(my_tree_unico.pdc_id8,0)::integer pdc_id8,
	COALESCE(my_tree_unico.codice_pdc8,'')::varchar codice_pdc8,
    COALESCE(my_tree_unico.descr_pdc8,'')::varchar descr_pdc8,
    COALESCE(my_tree_unico.pdc_id9,0)::integer pdc_id9,
	COALESCE(my_tree_unico.codice_pdc9,'')::varchar codice_pdc9,
    COALESCE(my_tree_unico.descr_pdc9,'')::varchar descr_pdc9,
    COALESCE(my_tree_unico.pdc_id10,0)::integer pdc_id1,
	COALESCE(my_tree_unico.codice_pdc10,'')::varchar codice_pdc10,
    COALESCE(my_tree_unico.descr_pdc10,'')::varchar descr_pdc10,
	my_tree_unico.livello::integer livello,     
    prime_note.prima_nota_id::integer prima_nota_id,
    prime_note.prima_nota_numero::integer prima_nota_numero,
    prime_note.prima_nota_data::date prima_nota_data,
    prime_note.prima_nota_progressivogiornale::integer prima_nota_progressivogiornale,
    prime_note.prima_nota_dataregistrazionegiornale::date prima_nota_dataregistrazionegiornale,
    prime_note.fonte_codice::varchar fonte_codice ,
    prime_note.fonte_descrizione::varchar fonte_descrizione,
    prime_note.evento_codice::varchar evento_codice,
    prime_note.evento_descrizione::varchar evento_descrizione,
    prime_note.causale_codice::varchar causale_codice,
    prime_note.causale_descrizione::varchar causale_descrizione,
    prime_note.prima_nota_stato_codice::varchar prima_nota_stato_codice,
    prime_note.prima_nota_stato_descrizione::varchar prima_nota_stato_descrizione,
    prime_note.codice_tipo_prima_nota::varchar codice_tipo_prima_nota,
    prime_note.desc_tipo_prima_nota::varchar desc_tipo_prima_nota,
    COALESCE(prime_note.elenco_attributi,'')::varchar elenco_attributi,
    COALESCE(importi_dare.importo_dare,0)::numeric importo_dare ,
    COALESCE(importi_avere.importo_avere,0)::numeric importo_avere,
    --Gli importi relativi al saldo iniziale sono impostati per tutti i record ma nel report
    --ne sara' preso solo 1 tramite FIRST invece che tramite SUM.
    importo_ini_dare::numeric importo_ini_dare,
    importo_ini_avere::numeric importo_ini_avere,	
    ''::varchar display_error
from my_tree_unico
	left join prime_note
    	on prime_note.pdc_id = my_tree_unico.pdc_id_foglia
    left join importi_avere
    	on importi_avere.prima_nota_id=prime_note.prima_nota_id
	left join importi_dare
    	on importi_dare.prima_nota_id=prime_note.prima_nota_id 
--where prime_note.prima_nota_id IS NOT NULL         
order by livello;



EXCEPTION
when no_data_found THEN
    raise notice 'Nessun dato trovato' ;
    display_error='Non ci sono prime note per il conto '||p_conto_pdc|| ' nel periodo indicato';
    return next;

END;
$body$;

  
CREATE OR REPLACE FUNCTION accrual.accrual_rep002_stampa_libro_giornale (
  p_ente_id integer,
  p_anno_bilancio varchar,
  p_code_tipo varchar,
  p_code_fonte varchar,
  p_code_evento varchar,
  p_code_causale varchar,
  p_numero_provvisorio integer,
  p_numero_definitivo integer,
  p_code_stato varchar,
  p_conto_pdc varchar,
  p_data_da_provv date,
  p_data_a_provv date,
  p_data_da_def date,
  p_data_a_def date
)
RETURNS TABLE (
  pdc_id integer,
  codice_pdc varchar,
  descr_pdc varchar,
  livello integer,
  prima_nota_id integer,
  numero_riga integer,
  prima_nota_numero integer,
  prima_nota_data date,
  prima_nota_progressivogiornale integer,
  prima_nota_dataregistrazionegiornale date,
  prima_nota_desc varchar,
  fonte_codice varchar,
  fonte_descrizione varchar,
  evento_codice varchar,
  evento_descrizione varchar,
  causale_codice varchar,
  causale_descrizione varchar,
  prima_nota_stato_codice varchar,
  prima_nota_stato_descrizione varchar,
  codice_tipo_prima_nota varchar,
  desc_tipo_prima_nota varchar,
  elenco_attributi varchar,
  importo_dare numeric,
  importo_avere numeric,
  display_error varchar
) LANGUAGE 'plpgsql'
VOLATILE
CALLED ON NULL INPUT
SECURITY INVOKER
PARALLEL UNSAFE
COST 100 ROWS 1000
AS
$body$
DECLARE
pdce_conto_id_in integer;
strQuery varchar;
strQueryParam varchar;
strQueryParamAttributi varchar;

BEGIN

/*
	27/02/2026.
    Versione 1 della procedura che estrae i dati del libro contabile per ACCRUAL.
    
    Parametri in input:
    - p_ente_id; OBBLIGATORIO; id dell'ente;
    - p_anno_bilancio; OBBLIGATORIO; anno del bilancio;
  	- p_code_tipo; OBBLIGATORIO; tipologia della prima nota; valori possibili:
    	- T; Tutte; nessun filtro sullo tipologia;
        - D: AUTOMATICA; 
        - P: MANUALE.
  	- p_code_fonte; OBBLIGATORIO; fonte; valori possibili:
    	- T; Tutte; nessun filtro sulla fonte;
        - A; Sistema Accrual;
        - C; Sitema Contabile;
        - I; Sistema Inventariale;        
  	- p_code_evento OBBLIGATORIO; eventi configurati dipendenti dalla scelta fatta per p_code_fonte; 
    		valori possibili:
    	- T; Tutti; nessun filtro sugli eventi;
        - tutti i possibili codici presenti nella tabella  accrual_t_evento;
  	- p_code_causale; OBBLIGATORIO; causali configurate dipendenti dalla scelta fatta per p_code_evento;
      		valori possibili:
    	- T; Tutte; nessun filtro sulle causali;
        - tutti i possibili codici presenti nella tabella accrual_t_causale;
  	- p_numero_provvisorio; FACOLTATIVO; numero provvisorio della prima nota;
  	- p_numero_definitivo FACOLTATIVO; numero definitivo della prima nota;
  	- p_code_stato OBBLIGATORIO; stato della prima nota; valori possibili:
    	- T; Tutti; tutte le prime note in stato NON ANNULLATO;
        - D; Definitiva;
        - P; Provvisorio;
  	- p_conto_pdc; FACOLTATIVO; codice del conto;
  	- p_data_da_provv; FACOLTATIVO; data provvisoria a partire dalla quale cercare le prime note (>=);
  	- p_data_a_provv; FACOLTATIVO; data provvisoria fino alla quale cercare le prime note (<=);
  	- p_data_da_def; FACOLTATIVO; data definitiva a partire dalla quale cercare le prime note (>=);
  	- p_data_a_def; FACOLTATIVO; data definitiva fino alla quale cercare le prime note (<=).
    
*/

pdce_conto_id_in:=0;

--se il conto e' stato specificato cerco l'id del conto in input.
if p_conto_pdc IS NOT NULL and p_conto_pdc <> '' then
  select pdc.pdc_id
      into pdce_conto_id_in
  from accrual_t_pdc pdc, accrual_d_pdc_classe pdc_classe
  where pdc_classe.pdc_classe_id=pdc.pdc_classe_id
  and pdc.ente_id = p_ente_id
  and pdc.pdc_codice_ufficiale = p_conto_pdc
  and pdc.fine_validita IS NULL
  and pdc.gest_data_del IS NULL
  and pdc_classe.fine_validita IS NULL
  and pdc_classe.gest_data_del IS NULL;
  
    --se non esiste restituisco un errore al report.
  IF NOT FOUND THEN
      display_error='Il codice PDC indicato ('||p_conto_pdc||') e'' inesistente';
      return next;
      return;
  END IF;
  
end if;


    
raise notice 'pdce_conto_id_in di % = %', p_conto_pdc,pdce_conto_id_in;

--controllo delle date se valorizzate.
if p_data_da_provv is not NULL and p_data_a_provv IS NOT NULL then
	if p_data_da_provv > p_data_a_provv then
      display_error='La data da provvisoria ('||to_char(p_data_da_provv,'dd/MM/yyyy') ||') non puo'' essere maggiore della data a ('||to_char(p_data_a_provv,'dd/MM/yyyy')||')';
      return next;
      return;
	END IF;	
end if;
    
if p_data_da_def is not NULL and p_data_a_def IS NOT NULL then
	if p_data_da_def > p_data_a_def then
      display_error='La data da definitiva ('||to_char(p_data_da_def,'dd/MM/yyyy') ||') non puo'' essere maggiore della data a ('||to_char(p_data_a_def,'dd/MM/yyyy')||')';
      return next;
      return;
	END IF;	
end if;

strQueryParam:='';
strQueryParamAttributi:='';

-- Visto che i parametri sono tanti, e' piu' comodo costrutire una query dinamica caricando 
--una stringa che conterra' la query completa da eseguire.

--costruisco la stringa dei parametri per il filtro della query in base a quali parametri sono stati 
--passati alla procedura.
--la stringa "E'\n" serve per mettere un ritorno a capo prima di ogni riga in modo da rendere
--piu' leggibile la query e non avere tutti i parametri in una sola riga.
if p_code_tipo <> 'T' THEN
	strQueryParam:= E'\n'||' and tipopn.prima_nota_tipo_codice ='''||p_code_tipo||'''';
end if;
if p_code_fonte <> 'T' THEN
	strQueryParam:= strQueryParam ||E'\n'||' and fonte.fonte_codice ='''||p_code_fonte||'''';
end if;
if p_code_evento <> 'T' THEN
	strQueryParam:= strQueryParam ||E'\n'||' and evento.evento_codice ='''||p_code_evento||'''';
end if;
if p_code_causale <> 'T' THEN
	strQueryParam:= strQueryParam ||E'\n'||' and cau.causale_codice ='''||p_code_causale||'''';
end if;
if p_numero_provvisorio IS NOT NULL AND p_numero_provvisorio <> 0 THEN
	strQueryParam:= strQueryParam ||E'\n'||' and pn.prima_nota_numero ='||p_numero_provvisorio;
end if;
if p_numero_definitivo IS NOT NULL AND p_numero_definitivo <> 0 THEN
	strQueryParam:= strQueryParam ||E'\n'||' and pn.prima_nota_progressivogiornale ='||p_numero_definitivo;
end if;
if p_code_stato <> 'T' THEN
	strQueryParam:= strQueryParam ||E'\n'||' and dstato.prima_nota_stato_codice ='''||p_code_stato||'''';
else     
	strQueryParam:= strQueryParam ||E'\n'||' and dstato.prima_nota_stato_codice <> ''A''';
end if;
--23/06/3-2026: se sono richieste le prime note provvisorie NON devo testare le date
--definitive DA/A anche se sono passate in input.
--if p_data_da_def IS NOT NULL THEN
if p_data_da_def IS NOT NULL and p_code_stato <> 'P' THEN
	strQueryParam:= strQueryParam ||E'\n'||' and ((date_trunc(''day'',pn.prima_nota_dataregistrazionegiornale) >='''||p_data_da_def||''' and
 	  dstato.prima_nota_stato_codice=''D'') OR dstato.prima_nota_stato_codice=''P'')';
end if;
--if p_data_a_def IS NOT NULL THEN
if p_data_a_def IS NOT NULL and p_code_stato <> 'P'  THEN
	strQueryParam:= strQueryParam ||E'\n'||' and ((date_trunc(''day'',pn.prima_nota_dataregistrazionegiornale) <='''||p_data_a_def||''' and
 	  dstato.prima_nota_stato_codice=''D'') OR dstato.prima_nota_stato_codice=''P'')';
end if;
if p_data_da_provv IS NOT NULL  THEN
	strQueryParam:= strQueryParam ||E'\n'||' and date_trunc(''day'',pn.prima_nota_data) >='''||p_data_da_provv||'''';
end if;
if p_data_a_provv IS NOT NULL  THEN
	strQueryParam:= strQueryParam ||E'\n'||' and date_trunc(''day'',pn.prima_nota_data) <='''||p_data_a_provv||'''';
end if;
if pdce_conto_id_in <> 0 THEN
	--se e' stato specificato il conto devo SOLO estrarre le prime note che hanno quel conto
    --pero' NON devo mettere il filtro su quel conto perche' la prima nota deve essere
    --estratta completa con tutti i suoi conti.
	strQueryParam:= strQueryParam ||E'\n'||'
	--verifico che sulle prime note esista almeno 1 importo valido per il conto in input.
    and exists (select 1  
    			from accrual_t_prima_nota_importo imp
                where imp.prima_nota_id=pn.prima_nota_id
                  and imp.ente_id ='|| p_ente_id||E'\n'
                  ||'and imp.pdc_id ='|| pdce_conto_id_in ||
                  ' and imp.gest_data_del is null)';
                  
   	--filtro sul conto per i paramentri.
   strQueryParamAttributi:= E'\n'||' and pdc.pdc_id =' ||pdce_conto_id_in;
else 
   strQueryParamAttributi:='';
end if;

                  
raise notice 'Parametri = % ', strQueryParam;

               
strQuery:='';

--costruisco la query aggiungendo anche la strQueryParam con i parametri per il filtro.
strQuery:='with prime_note as materialized (select pn.prima_nota_id, pn.prima_nota_numero, pn.prima_nota_data, 
  pn.prima_nota_progressivogiornale, pn.prima_nota_dataregistrazionegiornale, pn.prima_nota_descrizione,
  fonte.fonte_codice, fonte.fonte_descrizione, 
  evento.evento_codice, evento.evento_descrizione, cau.causale_codice, cau.causale_descrizione,
  dstato.prima_nota_stato_codice, dstato.prima_nota_stato_descrizione, 
  tipopn.prima_nota_tipo_codice codice_tipo_prima_nota, 
  tipopn.prima_nota_tipo_descrizione desc_tipo_prima_nota,
  --gli attributi possono essere legati alla prima nota direttamente dalla tabella
  --accrual_r_prima_nota_attributo oppure tramite la tabella degli importi delle prima nota 
  --accrual_t_prima_nota_importo.
  --Per questo motivo devo fare una UNION.
--28/05/2026: query degli attributi modificata per avere il nome dell''attributo una sola volta seguito
--da : e l''elenco dei valori. Inoltre tolto il legame con la tabella accrual_t_pdc che non serviva.
	(select string_agg(nome_attrib||'': ''||valori,'', '' order by nome_attrib)
		from (select attributo_descrizione nome_attrib,
	    	STRING_AGG(prima_nota_attributo_valore , '', '' ORDER BY prima_nota_attributo_valore) AS valori
		 	from (select attr.attributo_descrizione,rattr.prima_nota_attributo_valore 
		 		from accrual_d_attributo attr, 
		            accrual_r_prima_nota_attributo rattr,
		            accrual_t_prima_nota_importo imp	
					left join accrual_t_pdc pdc 
                    	on pdc.pdc_id = imp.pdc_id	           
		          where attr.attributo_id=rattr.attributo_id
		          and rattr.prima_nota_importo_id = imp.prima_nota_importo_id
		          and rattr.prima_nota_id=pn.prima_nota_id 
		          and rattr.ente_id ='|| p_ente_id ||strQueryParamAttributi||'               
		          and rattr.gest_data_del is null
		          and attr.gest_data_del is null
		    union 
		      select attr.attributo_descrizione,rattr.prima_nota_attributo_valore 
		        from accrual_d_attributo attr, 
		          accrual_r_prima_nota_attributo rattr,
		          accrual_t_prima_nota_importo imp		          
		        where attr.attributo_id=rattr.attributo_id
		            and imp.prima_nota_id=rattr.prima_nota_id
		            and rattr.prima_nota_id=pn.prima_nota_id
		            and rattr.ente_id = '|| p_ente_id ||'             		            
		            and rattr.gest_data_del is null
		            and attr.gest_data_del is null
		            and   rattr.prima_nota_importo_id IS NULL) tutto              
		  GROUP BY attributo_descrizione ) tutto1 
		) elenco_attributi    
from accrual_d_fonte fonte, accrual_t_evento evento, accrual_t_causale cau,
accrual_t_prima_nota pn, accrual_d_prima_nota_tipo tipopn,
accrual_r_prima_nota_stato rstato, accrual_d_prima_nota_stato dstato,
accrual_r_causale_stato r_cau_stato, accrual_d_causale_stato d_cau_stato
where cau.evento_id=evento.evento_id
	and pn.causale_id=cau.causale_id
	and pn.fonte_id=fonte.fonte_id
	and pn.prima_nota_tipo_id=tipopn.prima_nota_tipo_id
	and rstato.prima_nota_id=pn.prima_nota_id
	and dstato.prima_nota_stato_id=rstato.prima_nota_stato_id
    and r_cau_stato.causale_id=cau.causale_id
    and r_cau_stato.causale_stato_id=d_cau_stato.causale_stato_id
    and pn.ente_id = '|| p_ente_id ||'
    and pn.prima_nota_anno ='''|| p_anno_bilancio||'''::integer '||strQueryParam||'   
	and d_cau_stato.causale_stato_codice=''V''
    and fonte.gest_data_del is null
	and evento.gest_data_del is null
	and cau.gest_data_del is null
	and pn.gest_data_del is null
	and tipopn.gest_data_del is null
	and rstato.gest_data_del is null
    and rstato.fine_validita is null
	and dstato.gest_data_del is null
    and r_cau_stato.fine_validita IS NULL
    and r_cau_stato.gest_data_del IS NULL),
importi as materialized (    				  
    	select pnimp.prima_nota_id, pdc.pdc_id, pdc.pdc_codice_ufficiale, pdc.pdc_descrizione,
        	pdc.pdc_livello livello, pnimp.prima_nota_importo_segno segno, 
            --sum(pnimp.prima_nota_importo_importo) importo  
        	pnimp.prima_nota_importo_importo importo       	
        from accrual_t_prima_nota_importo pnimp, accrual_t_pdc pdc
        where pdc.pdc_id=pnimp.pdc_id
            and pnimp.ente_id = '|| p_ente_id ||'               
            and pnimp.gest_data_del is null
            and pdc.gest_data_del is null
        order by pnimp.prima_nota_id, pdc.pdc_codice_ufficiale)
        --group by pnimp.prima_nota_id, pdc.pdc_id, --pdc.pdc_codice_ufficiale, 
        		--pdc.pdc_descrizione, 
         --       pdc.pdc_livello, pnimp.prima_nota_importo_segno)           
select 
	importi.pdc_id::integer pdc_id, 
    importi.pdc_codice_ufficiale::varchar codice_pdc, 
    importi.pdc_descrizione::varchar descr_pdc,
  	importi.livello::integer livello,
    prime_note.prima_nota_id::integer,
    ((ROW_NUMBER() OVER(PARTITION BY prime_note.prima_nota_id))-1)::integer numero_riga,
    prime_note.prima_nota_numero::integer prima_nota_numero,
    prime_note.prima_nota_data::date prima_nota_data,
    COALESCE(prime_note.prima_nota_progressivogiornale,0)::integer prima_nota_progressivogiornale,    
    prime_note.prima_nota_dataregistrazionegiornale::date prima_nota_dataregistrazionegiornale,
    prime_note.prima_nota_descrizione::varchar prima_nota_desc,
    prime_note.fonte_codice::varchar fonte_codice,
    prime_note.fonte_descrizione::varchar fonte_descrizione,
    prime_note.evento_codice::varchar evento_codice,
    prime_note.evento_descrizione::varchar evento_descrizione,
    prime_note.causale_codice::varchar causale_codice,
    prime_note.causale_descrizione::varchar causale_descrizione,
    prime_note.prima_nota_stato_codice::varchar prima_nota_stato_codice,
    prime_note.prima_nota_stato_descrizione::varchar prima_nota_stato_descrizione,
    prime_note.codice_tipo_prima_nota::varchar codice_tipo_prima_nota,
    prime_note.desc_tipo_prima_nota::varchar desc_tipo_prima_nota,
    COALESCE(prime_note.elenco_attributi,'''')::varchar elenco_attributi,
    case when importi.segno = ''DARE'' then COALESCE(importi.importo,0)::numeric
    	else 0::numeric end importo_dare,
    case when importi.segno = ''AVERE'' then COALESCE(importi.importo,0)::numeric
    	else 0::numeric end importo_avere,
    ''''::varchar display_error
from prime_note
	left join importi 
    	on importi.prima_nota_id=prime_note.prima_nota_id';
    	

raise notice 'strQuery = %', strQuery;

return query execute strQuery;


EXCEPTION
when no_data_found THEN
    raise notice 'Nessun dato trovato' ;
    display_error='Non ci sono prime note per i parametri specificati';
    return next;

END;
$body$;

CREATE OR REPLACE FUNCTION accrual.accrual_rep003_stampa_bilancio_verifica (
  p_ente_id integer,
  p_anno_bilancio varchar,
  p_tipo_stampa varchar,
  p_stato_pn varchar,
  p_stampa_seg_utente varchar
)
RETURNS TABLE (
  tipo_report varchar,
  tipo_conto_patrimoniale varchar,
  classe_conto varchar,
  pdce_id integer,
  pdce_id_padre integer,
  pdce_code varchar,
  pdce_desc varchar,
  pdce_livello integer,
  livello_mostrato varchar,
  pdc_collegamento_bil_itas varchar,
  importo_dare numeric,
  importo_avere numeric,
  livello_importo integer,
  prima_nota_id integer,
  tipo_prima_nota varchar,
  stato_prima_nota varchar,
  ordinamento varchar,
  ordinamento_num integer,
  display_error varchar
) LANGUAGE 'plpgsql'
VOLATILE
CALLED ON NULL INPUT
SECURITY INVOKER
PARALLEL UNSAFE
COST 100 ROWS 1000
AS
$body$
DECLARE


BEGIN

/*
	06/03/2026.
    Versione 1 della procedura che estrae i dati del bilancio di verifica per ACCRUAL.
    
    Parametri in input:
    - p_ente_id; OBBLIGATORIO; id dell'ente;
    - p_anno_bilancio; OBBLIGATORIO; anno del bilancio;
    - p_tipo_stampa; OBBLIGATORIO; tipo di stampa; valori possibili:
    	- CE; stampa report dei Conti Economici;
        - CP; stampa report dei Conti Patrimoniali.
    - p_stato_pn; OBBLIGATORIO; stato della prima Nota; valori possibili:
    	- T; Tutti; nessun filtro sullo stato (sono escluse le annullate);
        - D: Definitiva; stampa solo dei dati delle prime note in stato definitivo;
        - P: Provvisorio; stampa solo dei dati delle prime note in stato Provvisorio.
    	
        Aggiunto il 15/05/2026:
    - p_stampa_seg_utente; OBBLIGATORIO; valori possibili:
    	- SI; sono estratti anche i dati della famiglia 'ACCRUAL-UTENTE';
        - NO; sono esclusi i dati della famiglia 'ACCRUAL-UTENTE'; estratte solo 
        	le famiglie 'ACCRUAL-A','ACCRUAL-B'.
   
    Per la stampa Conti Patrimoniali a procedura estrae i dati dei conti con classe:
    	- classe AP = ATTIVO CONTI PATRIMONIALI
		- classe PP = PASSIVO CONTI PATRIMONIALI.
    Per la stampa Conti Economici a procedura estrae i dati dei conti con classe:
    	- classe CE =  CONTI ECONOMICI
		- classe RE =  CONTI ECONOMICI

    Per entrambe le tipologie di report sono estratti i dati dei conti che appartengono alle famiglie:
    	- ACCRUAL-A; 
        - ACCRUAL-B;
        - ACCRUAL-UTENTE (solo se p_stampa_seg_utente = 'SI').
        
    Gli importi devono essere calcolati nel report secondo la seguente regola:
    	- Conti Patrimoniali:
        	- CONTI DI TIPO AP = DARE - AVERE;
            - CONTI DI TIPO PP = AVERE- DARE.
        - Conti Economici:
        	- CONTI DI TIPO CE = DARE -AVERE;
			- CONTI DI TIPO RE = AVERE - DARE.

*/


return query
	--estraggo in modo ricorsivo i dati dei conti
WITH RECURSIVE albero AS materialized (
    -- RADICI
    SELECT fam.pdc_famiglia_codice,
        pdc.pdc_id,
        pdc.pdc_codice_ufficiale,
        pdc.pdc_descrizione,
        pdc.pdc_id_padre,
        pdc.pdc_livello,
        classe.pdc_classe_codice,
        pdc.pdc_collegamento_bil_itas,
        	--e' necessario trattare pdc_codice_ufficiale in un array di numeri interi per
            --permettere l'ordinamento corretto.
        string_to_array(pdc.pdc_codice_ufficiale,'.')::int[] AS path
    FROM accrual_t_pdc pdc
    JOIN accrual_t_pdc_famiglia fam
        ON fam.pdc_famiglia_id = pdc.pdc_famiglia_id
    JOIN accrual_d_pdc_classe classe
        ON classe.pdc_classe_id = pdc.pdc_classe_id
    WHERE pdc.ente_id = p_ente_id
      AND pdc.pdc_livello = 1
	  AND fam.pdc_famiglia_codice IN ('ACCRUAL-A','ACCRUAL-B','ACCRUAL-UTENTE')      	
      AND pdc.gest_data_del IS NULL
      AND fam.gest_data_del IS NULL
      AND classe.gest_data_del IS NULL
    UNION ALL
    -- FIGLI
    SELECT 
		fam_figli.pdc_famiglia_codice,
        figli.pdc_id,
        figli.pdc_codice_ufficiale,
        figli.pdc_descrizione,
        figli.pdc_id_padre,
        figli.pdc_livello,
        classe.pdc_classe_codice,
        figli.pdc_collegamento_bil_itas,
        alb.path || string_to_array(figli.pdc_codice_ufficiale,'.')::int[]
    FROM accrual_t_pdc figli
		JOIN accrual_t_pdc_famiglia fam_figli
			ON fam_figli.pdc_famiglia_id = figli.pdc_famiglia_id
	    JOIN albero alb
	        ON figli.pdc_id_padre = alb.pdc_id
	    JOIN accrual_d_pdc_classe classe
	        ON classe.pdc_classe_id = figli.pdc_classe_id
),
	--dati delle prime note e degli importi.
importi as materialized (    				  
    	select pnimp.prima_nota_id, pdc.pdc_id, pdc.pdc_livello livello,
        	pnimp.prima_nota_importo_segno, pn_tipo.prima_nota_tipo_codice tipo_prima_nota,
            dstato.prima_nota_stato_codice,
            sum(pnimp.prima_nota_importo_importo) importo         	
        from accrual_t_prima_nota_importo pnimp, accrual_t_pdc pdc,
        	accrual_t_prima_nota pn, accrual_d_prima_nota_tipo pn_tipo,
            accrual_r_prima_nota_stato rstato, 
    		accrual_d_prima_nota_stato dstato
        where pdc.pdc_id=pnimp.pdc_id
        	and pn.prima_nota_id=pnimp.prima_nota_id
            and pn.prima_nota_tipo_id=pn_tipo.prima_nota_tipo_id
            and rstato.prima_nota_id=pn.prima_nota_id
            and rstato.prima_nota_stato_id=dstato.prima_nota_stato_id
            and pnimp.ente_id =  p_ente_id  
            and pn.prima_nota_anno = p_anno_bilancio::integer
            and ((p_stato_pn = 'T' and dstato.prima_nota_stato_codice <> 'A') OR
       			 (p_stato_pn <> 'T' and dstato.prima_nota_stato_codice = p_stato_pn))                    
            and pnimp.gest_data_del is null
            and pdc.gest_data_del is null
            and pn.gest_data_del is null 
            and rstato.gest_data_del is null
  			and rstato.fine_validita is null           
        group by pnimp.prima_nota_id, pdc.pdc_id, pdc.pdc_livello, dstato.prima_nota_stato_codice,
        	pn_tipo.prima_nota_tipo_codice, pnimp.prima_nota_importo_segno)    
SELECT case when albero.pdc_classe_codice in('AP','PP') 
    then 'CONTI_PATRIMONIALI'::varchar
    else 'CONTI_ECONOMICI'::varchar end tipo_report,
  case when albero.pdc_classe_codice in('AP')
      then 'ATTIVO'::varchar 
      else case when albero.pdc_classe_codice in('PP')
          then 'PASSIVO'::varchar 
              --nel caso dei CONTI_ECONOMICI il tipo_conto_patrimoniale non e' significativo.
        else ''::varchar end end tipo_conto_patrimoniale,            	
  albero.pdc_classe_codice::varchar classe_conto,
  albero.pdc_id::integer pdce_id,
  albero.pdc_id_padre::integer pdce_id_padre,
  albero.pdc_codice_ufficiale::varchar pdce_code,
  albero.pdc_descrizione::VARCHAR pdce_desc,
  albero.pdc_livello::integer pdce_livello,  
  CASE albero.pdc_livello
        WHEN 1 THEN 'I'::varchar
        WHEN 2 THEN 'II'::varchar
        WHEN 3 THEN 'III'::varchar
        WHEN 4 THEN 'IV'::varchar
        WHEN 5 THEN 'V'::varchar
        WHEN 6 THEN 'VI'::varchar
        WHEN 7 THEN 'VII'::varchar
        WHEN 8 THEN 'VIII'::varchar
        WHEN 9 THEN 'IX'::varchar
        WHEN 10 THEN 'X'::varchar end livello_mostrato,
  COALESCE(albero.pdc_collegamento_bil_itas,'')::varchar pdc_collegamento_bil_itas,
  case when importi.prima_nota_importo_segno ='DARE'
  	then COALESCE(importi.importo,0)::numeric
    else 0::numeric end importo_dare,
  case when importi.prima_nota_importo_segno ='AVERE'
  	then COALESCE(importi.importo,0)::numeric 
    else 0::numeric end importo_avere,
  COALESCE(importi.livello,0)::integer livello_importo,
  COALESCE(importi.prima_nota_id,0)::integer prima_nota_id,
  COALESCE(importi.tipo_prima_nota,'')::varchar tipo_prima_nota,
  COALESCE(importi.prima_nota_stato_codice,'')::varchar stato_prima_nota,
  albero.path::varchar ordinamento, 
  	--ordinamento_num serve per permettere l'ordinamento corretto nel report.
  ROW_NUMBER() OVER (order by albero.path)::integer ordinamento_num,
  ''::varchar display_error          
from albero
	left join importi
    	on importi.pdc_id = albero.pdc_id
where --se e' richiesta la stampa dei Conti Economici (CE) estraggo solo le classi 'CE','RE'
	  --se e' richiesta la stampa dei Conti Patrimoniali (CP) estraggo solo le classi 'AP','PP'
	((p_tipo_stampa = 'CE' and albero.pdc_classe_codice in('CE','RE')) OR
	 (p_tipo_stampa = 'CP' and albero.pdc_classe_codice in('AP','PP'))) AND
    	--18/05/2026: aggiunto filtro sulla famiglia se richiesto in input.
     ((p_stampa_seg_utente ='SI' AND 
      			albero.pdc_famiglia_codice IN ('ACCRUAL-A','ACCRUAL-B','ACCRUAL-UTENTE')) OR
      	 	p_stampa_seg_utente ='NO' AND albero.pdc_famiglia_codice IN ('ACCRUAL-A','ACCRUAL-B'))   
ORDER BY albero.path; 
          
EXCEPTION
when no_data_found THEN
    raise notice 'Nessun dato trovato' ;
    display_error='Non ci sono prime note per i parametri specificati';
    return next;

END;
$body$;
  
/* i conti di famiglia ACCRUAL con descrizione %fond%ammortamento% sono nati con la classe AP, vanno tutti spostati in classe PP */  
update accrual_t_pdc pdc
set pdc_classe_id=pdcclasse_pp.pdc_classe_id,gest_data_upd=now(),gest_utente_upd='admin'
from accrual_t_ente e, accrual_t_pdc_famiglia fam, accrual_d_pdc_classe pdcclasse, accrual_d_pdc_tipo pdctipo, accrual_d_pdc_stato pdcstato,
accrual_d_pdc_classe pdcclasse_pp
where pdc.ente_id=e.ente_id
and fam.pdc_famiglia_id=pdc.pdc_famiglia_id
and pdcclasse.pdc_classe_id=pdc.pdc_classe_id
and pdctipo.pdc_tipo_id=pdc.pdc_tipo_id
and pdcstato.pdc_stato_id=pdc.pdc_stato_id
and pdcclasse_pp.pdc_classe_codice='PP'
and pdc.pdc_descrizione ilike '%fond%ammortamento%'
and fam.pdc_famiglia_codice like 'ACCRUAL%'
and pdcclasse.pdc_classe_codice='AP'
and pdc.gest_data_del is null 
and fam.gest_data_del is null 
and pdcclasse.gest_data_del is null 
and pdctipo.gest_data_del is null 
and pdcstato.gest_data_del is null 
and pdcclasse_pp.gest_data_del is null;
   
/* SPECIFICO PER ENTI CSI COMPRESO ENTE TEST: se si vuole applicare la configurazioni ad altri enti occorre inserire il codice_univoco dell'ente che interessa - INIZIO */
update accrual_t_pdc pdc
set pdc_foglia_non_foglia=true,gest_data_upd=now(),gest_utente_upd='admin'
from accrual_t_ente e,  accrual_t_pdc_famiglia fam, accrual_d_pdc_classe pdcclasse, accrual_d_pdc_tipo pdctipo, accrual_d_pdc_stato pdcstato
where pdc.ente_id=e.ente_id
and fam.pdc_famiglia_id=pdc.pdc_famiglia_id
and pdcclasse.pdc_classe_id=pdc.pdc_classe_id
and pdctipo.pdc_tipo_id=pdc.pdc_tipo_id
and pdcstato.pdc_stato_id=pdc.pdc_stato_id
and fam.pdc_famiglia_codice ilike 'ACCRUAL-B%'
and e.ente_codice_univoco in ('ENTE_TEST','80087670016','01907990012','97603810017','92116650349','97694170016','01550320061')
and pdc.pdc_foglia_non_foglia=false
and pdc.gest_data_del is null 
and fam.gest_data_del is null 
and pdcclasse.gest_data_del is null 
and pdctipo.gest_data_del is null 
and pdcstato.gest_data_del is null 
and exists (select 1 from accrual_t_pdc pdc2, accrual_t_pdc_famiglia fam2
where pdc2.ente_id=e.ente_id
and fam2.pdc_famiglia_id=pdc2.pdc_famiglia_id
and fam2.pdc_famiglia_codice ilike 'ACCRUAL-B%'
and pdc2.pdc_id_padre=pdc.pdc_id
and pdc2.gest_data_del is null 
and fam2.gest_data_del is null);
/* SPECIFICO PER ENTI CSI COMPRESO ENTE TEST: se si vuole applicare la configurazioni ad altri enti occorre inserire il codice_univoco dell'ente che interessa - FINE */

  