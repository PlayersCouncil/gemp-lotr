package com.gempukku.lotro.cards.official.set06;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_06_087_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("nertea", "6_87");
					// Úlairë Nelya, Ringwraith in Twilight: a [wraith] Nazgûl, cost 5, site 2; fierce and damage +1 only
					// at 2 burdens or 2 wounds on the Ring-bearer
					put("nelya", "2_84");
					// Ulaire Attea, Khamul of the East: a [raider] Nazgûl; his texts read burdens only
					put("khamul", "103_61");
					// Morgul Hound: a [wraith] minion who is no Nazgûl; stronger only at 2 wounds on the Ring-bearer
					put("hound", "7_192");
					// Gollum, Stinker: a [gollum] Shadow card, which shows no Free Peoples culture
					put("gollum", "5_25");
					put("sam", "1_311");        // Sam, Son of Hamfast: a Ring-bound [shire] companion who is not the Ring-bearer
					// Sméagol, Hurried Guide: a Ring-bound [gollum] companion who is no Hobbit; a skirmish ability only
					put("smeagol", "7_72");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: an unbound [gondor] companion
					put("legolas", "1_50");     // Legolas, Greenleaf: an unbound [elven] companion
					put("gimli", "1_13");       // Gimli, Son of Glóin: an unbound [dwarven] companion
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void UlaireNerteaStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 6
		 * Name: Úlairë Nertëa, Winged Hunter
		 * Unique: True
		 * Side: Shadow
		 * Culture: Wraith
		 * Twilight Cost: 4
		 * Type: Minion
		 * Subtype: Nazgûl
		 * Strength: 9
		 * Vitality: 2
		 * Site Number: 3
		 * Game Text: When you play Úlairë Nertëa, spot another Nazgûl to make the Free Peoples player exert a Ring-bound companion for each Free Peoples culture you can spot over 2.
		*/

		var scn = GetScenario();

		var nertea = scn.GetFreepsCard("nertea");

		assertEquals("Úlairë Nertëa", nertea.getBlueprint().getTitle());
		assertEquals("Winged Hunter", nertea.getBlueprint().getSubtitle());
		assertTrue(nertea.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, nertea.getBlueprint().getSide());
		assertEquals(Culture.WRAITH, nertea.getBlueprint().getCulture());
		assertEquals(CardType.MINION, nertea.getBlueprint().getCardType());
		assertEquals(Race.NAZGUL, nertea.getBlueprint().getRace());
		assertEquals(4, nertea.getBlueprint().getTwilightCost());
		assertEquals(9, nertea.getBlueprint().getStrength());
		assertEquals(2, nertea.getBlueprint().getVitality());
		assertEquals(3, nertea.getBlueprint().getSiteNumber());
	}

	@Test
	public void WithThreeFreePeoplesCulturesTheFreePeoplesPlayerExertsOneRingBoundCompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var nertea = scn.GetShadowCard("nertea");
		var nelya = scn.GetShadowCard("nelya");
		var frodo = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		var smeagol = scn.GetFreepsCard("smeagol");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCardsToHand(nertea);
		// Three Free Peoples cultures: [shire] (Frodo, Sam), [gondor] (Aragorn) and [gollum] (Sméagol).
		scn.MoveCompanionsToTable(sam, smeagol, aragorn);
		assertInHand(nertea);

		scn.StartGame();
		// Sam exhausted (vitality 4, 3 wounds).
		scn.AddWoundsToChar(sam, 3);
		assertEquals(3, scn.GetWoundsOn(sam));
		scn.MoveMinionsToTable(nelya);
		scn.FreepsPass();
		scn.SetTwilight(20);
		scn.ShadowRefreshDecision();
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(3, scn.GetWoundsOn(sam));
		assertEquals(0, scn.GetWoundsOn(smeagol));
		assertEquals(0, scn.GetWoundsOn(aragorn));

		scn.ShadowPlayCard(nertea);
		assertInZone(Zone.SHADOW_CHARACTERS, nertea);

		// The Free Peoples player chooses: Ring-bound companions who can exert; not the exhausted Sam, the unbound
		// Aragorn or a minion.
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, smeagol));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(sam, aragorn, nelya, nertea));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(smeagol);

		// One culture over 2: one exertion, and nothing more is asked.
		assertEquals(1, scn.GetWoundsOn(smeagol));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(3, scn.GetWoundsOn(sam));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(nelya));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithFourCulturesTheRingBearerAloneExertsTwice() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var nertea = scn.GetShadowCard("nertea");
		var khamul = scn.GetShadowCard("khamul");
		var gollum = scn.GetShadowCard("gollum");
		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var legolas = scn.GetFreepsCard("legolas");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCardsToHand(nertea);
		// [shire], [gondor], [elven] and [dwarven]: two over 2.
		scn.MoveCompanionsToTable(aragorn, legolas, gimli);
		assertInHand(nertea);

		scn.StartGame();
		// Gollum is a [gollum] Shadow card: if he were counted as a culture there would be three exertions.
		scn.MoveMinionsToTable(khamul, gollum);
		scn.FreepsPass();
		scn.SetTwilight(20);
		scn.ShadowRefreshDecision();
		assertEquals(0, scn.GetWoundsOn(frodo));

		scn.ShadowPlayCard(nertea);

		// Frodo is the only Ring-bound companion, and the exertions may fall on one companion (CR 4.2 "for each").
		assertEquals(2, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(legolas));
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithTwoCulturesNobodyExerts() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var nertea = scn.GetShadowCard("nertea");
		var nelya = scn.GetShadowCard("nelya");
		var frodo = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCardsToHand(nertea);
		// [shire] twice and [gondor]: two cultures, none over 2.
		scn.MoveCompanionsToTable(sam, aragorn);
		assertInHand(nertea);

		scn.StartGame();
		scn.MoveMinionsToTable(nelya);
		scn.FreepsPass();
		scn.SetTwilight(20);
		scn.ShadowRefreshDecision();
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(sam));

		scn.ShadowPlayCard(nertea);

		assertInZone(Zone.SHADOW_CHARACTERS, nertea);
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(sam));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithNoOtherNazgulNobodyExertsAndANazgulPlayedAfterSetsNothingOff() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var nertea = scn.GetShadowCard("nertea");
		var nelya = scn.GetShadowCard("nelya");
		var hound = scn.GetShadowCard("hound");
		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var legolas = scn.GetFreepsCard("legolas");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCardsToHand(nertea, nelya);
		// Four cultures, so two exertions would be owed.
		scn.MoveCompanionsToTable(aragorn, legolas, gimli);
		assertInHand(nertea, nelya);

		scn.StartGame();
		// A [wraith] minion who is no Nazgûl; Nertëa herself is a Nazgûl, but not "another".
		scn.MoveMinionsToTable(hound);
		scn.FreepsPass();
		scn.SetTwilight(20);
		scn.ShadowRefreshDecision();
		assertEquals(0, scn.GetWoundsOn(frodo));

		scn.ShadowPlayCard(nertea);
		assertInZone(Zone.SHADOW_CHARACTERS, nertea);
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertTrue(scn.AwaitingShadowPhaseActions());

		// Another Nazgûl played after her is no play of Nertëa: nothing is set off.
		scn.ShadowPlayCard(nelya);
		assertInZone(Zone.SHADOW_CHARACTERS, nelya);
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
