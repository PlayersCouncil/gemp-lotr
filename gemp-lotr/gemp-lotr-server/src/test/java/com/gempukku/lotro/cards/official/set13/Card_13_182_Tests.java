package com.gempukku.lotro.cards.official.set13;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_13_182_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("enquea", "13_182");
					// Úlairë Lemenya, Lieutenant of Morgul: [wraith] Nazgûl; her text reaches only bearers of a
					// [wraith] condition, and none is laid
					put("lemenya", "1_232");
					// Úlairë Attëa, Khamûl of the East: a [raider] Nazgûl; his texts need 3 or 5 burdens
					put("khamul", "103_61");
					// Morgul Hound: a [wraith] Orc, no Nazgûl; strength +4 and fierce only at 2 wounds on the Ring-bearer
					put("hound", "7_192");
					// Úlairë Nelya, Ringwraith in Twilight: [wraith] Nazgûl, site 2; fierce and damage +1 only at 2
					// burdens or 2 wounds on the Ring-bearer
					put("nelya", "2_84");

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man, unbound; a maneuver ability used only on purpose
					put("gimli", "1_13");       // Gimli, Son of Gloin: [dwarven] Dwarf, unbound; a skirmish ability used only on purpose
					put("merry", "1_302");      // Merry, Friend to Sam: [shire] Hobbit, unbound; a skirmish ability used only on purpose
					put("pippin", "1_306");     // Pippin, Friend to Frodo: [shire] Hobbit, unbound; his text is about [shire] tales
					// Sam, Son of Hamfast: Ring-bound, and not the Ring-bearer; a fellowship ability and a response
					// to Frodo's death
					put("sam", "1_311");
					// Bounder: [shire] Hobbit ally, no companion; a skirmish ability used only on purpose
					put("bounder", "1_286");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void UlaireEnqueaStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 13
		 * Name: Úlairë Enquëa, Sixth of the Nine Riders
		 * Unique: True
		 * Side: Shadow
		 * Culture: Wraith
		 * Twilight Cost: 6
		 * Type: Minion
		 * Subtype: Nazgûl
		 * Strength: 11
		 * Vitality: 4
		 * Site Number: 3
		 * Game Text: <b>Fierce</b>.<br>When you play Úlairë Enquëa, if you can spot 2 other Nazgûl, you may exert 2 unbound companions (or exert each companion if you can spot 6 companions).
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("enquea");

		assertEquals("Úlairë Enquëa", card.getBlueprint().getTitle());
		assertEquals("Sixth of the Nine Riders", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.WRAITH, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.NAZGUL, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.FIERCE));
		assertEquals(6, card.getBlueprint().getTwilightCost());
		assertEquals(11, card.getBlueprint().getStrength());
		assertEquals(4, card.getBlueprint().getVitality());
		assertEquals(3, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void PlayedWithTwoOtherNazgulHeExertsTwoUnboundCompanionsOfHisPlayersChoice() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var merry = scn.GetFreepsCard("merry");
		var sam = scn.GetFreepsCard("sam");
		var bounder = scn.GetFreepsCard("bounder");
		// Five companions with Frodo, one short of six, and an ally beside them.
		scn.MoveCompanionsToTable(aragorn, gimli, merry, sam);
		scn.MoveAlliesToTable(bounder);

		var enquea = scn.GetShadowCard("enquea");
		var lemenya = scn.GetShadowCard("lemenya");
		var khamul = scn.GetShadowCard("khamul");
		scn.MoveCardsToHand(enquea);

		scn.StartGame();
		scn.SetTwilight(20);
		// Two other Nazgûl, of two cultures.
		scn.MoveMinionsToTable(lemenya, khamul);
		scn.SkipToPhase(Phase.SHADOW);

		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(merry));
		assertEquals(0, scn.GetWoundsOn(bounder));
		scn.ShadowPlayCard(enquea);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(enquea));
		scn.ShadowAcceptOptionalTrigger();

		// The unbound companions, a Man, a Dwarf and a Hobbit; not the Ring-bearer, not Sam (Ring-bound), not
		// the ally. Five companions are one short of six: the choice of two is asked, and nobody is exerted yet.
		assertTrue(scn.ShadowHasCardChoiceAvailable(aragorn, gimli, merry));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(frodo, sam, bounder));
		assertEquals(2, scn.ShadowGetChoiceMin());
		assertEquals(2, scn.ShadowGetChoiceMax());
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(sam));

		scn.ShadowChooseCards(aragorn, merry);

		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(merry));
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(sam));
		assertEquals(0, scn.GetWoundsOn(bounder));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithOneOtherNazgulAndAWraithOrcHeOffersNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(aragorn, gimli);

		var enquea = scn.GetShadowCard("enquea");
		var lemenya = scn.GetShadowCard("lemenya");
		var hound = scn.GetShadowCard("hound");
		scn.MoveCardsToHand(enquea);

		scn.StartGame();
		scn.SetTwilight(20);
		// Lemenya is the one other Nazgûl; the Hound is a [wraith] minion and no Nazgûl, and Enquëa himself is
		// not "other".
		scn.MoveMinionsToTable(lemenya, hound);
		scn.SkipToPhase(Phase.SHADOW);

		scn.ShadowPlayCard(enquea);

		assertFalse(scn.ShadowHasOptionalTriggerAvailable(enquea));
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(gimli));
	}

	@Test
	public void DeclinedNothingIsExertedAndANazgulPlayedAfterHimOffersNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var merry = scn.GetFreepsCard("merry");
		scn.MoveCompanionsToTable(aragorn, gimli, merry);

		var enquea = scn.GetShadowCard("enquea");
		var lemenya = scn.GetShadowCard("lemenya");
		var khamul = scn.GetShadowCard("khamul");
		var nelya = scn.GetShadowCard("nelya");
		scn.MoveCardsToHand(enquea, nelya);

		scn.StartGame();
		scn.SetTwilight(20);
		scn.MoveMinionsToTable(lemenya, khamul);
		scn.SkipToPhase(Phase.SHADOW);

		assertEquals(0, scn.GetWoundsOn(scn.GetRingBearer()));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(merry));
		scn.ShadowPlayCard(enquea);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(enquea));
		scn.ShadowDeclineOptionalTrigger();

		assertEquals(0, scn.GetWoundsOn(scn.GetRingBearer()));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(merry));
		assertTrue(scn.AwaitingShadowPhaseActions());

		// Another Nazgûl played while Enquëa is in play, with two others still to spot: his text answers his
		// own play only.
		scn.ShadowPlayCard(nelya);

		assertFalse(scn.ShadowHasOptionalTriggerAvailable(enquea));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(merry));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithSixCompanionsEachCompanionIsExertedAndNoneIsChosen() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var merry = scn.GetFreepsCard("merry");
		var pippin = scn.GetFreepsCard("pippin");
		var sam = scn.GetFreepsCard("sam");
		var bounder = scn.GetFreepsCard("bounder");
		// Six companions with Frodo, two of them Ring-bound, and an ally.
		scn.MoveCompanionsToTable(aragorn, gimli, merry, pippin, sam);
		scn.MoveAlliesToTable(bounder);

		var enquea = scn.GetShadowCard("enquea");
		var lemenya = scn.GetShadowCard("lemenya");
		var khamul = scn.GetShadowCard("khamul");
		scn.MoveCardsToHand(enquea);

		scn.StartGame();
		scn.SetTwilight(20);
		scn.MoveMinionsToTable(lemenya, khamul);
		scn.SkipToPhase(Phase.SHADOW);

		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(sam));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(merry));
		assertEquals(0, scn.GetWoundsOn(pippin));
		assertEquals(0, scn.GetWoundsOn(bounder));
		assertEquals(0, scn.GetWoundsOn(lemenya));
		assertEquals(0, scn.GetWoundsOn(khamul));
		scn.ShadowPlayCard(enquea);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(enquea));
		scn.ShadowAcceptOptionalTrigger();

		// The parenthesis is taken whenever it can be (Q-0035): nobody is chosen, and every companion exerts,
		// the Ring-bearer and Sam among them. The ally and the minions do not.
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetWoundsOn(sam));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertEquals(1, scn.GetWoundsOn(merry));
		assertEquals(1, scn.GetWoundsOn(pippin));
		assertEquals(0, scn.GetWoundsOn(bounder));
		assertEquals(0, scn.GetWoundsOn(lemenya));
		assertEquals(0, scn.GetWoundsOn(khamul));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithNoUnboundCompanionAbleToExertHeOffersNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var sam = scn.GetFreepsCard("sam");
		var bounder = scn.GetFreepsCard("bounder");
		// Frodo and Sam are Ring-bound, Aragorn is unbound and exhausted, and Bounder is no companion.
		scn.MoveCompanionsToTable(aragorn, sam);
		scn.MoveAlliesToTable(bounder);

		var enquea = scn.GetShadowCard("enquea");
		var lemenya = scn.GetShadowCard("lemenya");
		var khamul = scn.GetShadowCard("khamul");
		scn.MoveCardsToHand(enquea);

		scn.StartGame();
		scn.AddWoundsToChar(aragorn, 3);
		scn.SetTwilight(20);
		scn.MoveMinionsToTable(lemenya, khamul);
		scn.SkipToPhase(Phase.SHADOW);

		scn.ShadowPlayCard(enquea);

		assertFalse(scn.ShadowHasOptionalTriggerAvailable(enquea));
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertEquals(3, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(sam));
	}
}
