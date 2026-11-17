package com.gempukku.lotro.cards.unofficial.rtmd.set_92_rotk;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.assertInZone;
import static org.junit.Assert.*;

public class Card_92_020_Tests
{
	protected VirtualTableScenario GetFreepsScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("enquea", "1_231");     // Úlairë Enquëa, Lieutenant of Morgul: [wraith] Nazgul, cost 6, strength 11, vitality 4, site 3; fierce
					put("attea", "1_229");      // Úlairë Attëa, Keeper of Dol Guldur: [wraith] Nazgul, cost 6, strength 12, vitality 3, site 3; fierce
					put("lemenya", "1_232");    // Úlairë Lemenya, Lieutenant of Morgul: [wraith] Nazgul, cost 4, strength 9, vitality 2, site 3
					put("nelya", "2_84");       // Úlairë Nelya, Ringwraith in Twilight: [wraith] Nazgul, cost 5, vitality 3, site 2; twilight
					put("otsea", "1_235");      // Úlairë Otsëa, Lieutenant of Morgul: [wraith] Nazgul, cost 4, vitality 3, site 3
					put("toldea", "1_236");     // Úlairë Toldëa, Messenger of Morgul: [wraith] Nazgul, cost 6, vitality 3, site 3; fierce
					// The only Nazgul of another culture.  His title is Attëa's: the two are never in play together.
					put("khamul", "103_61");    // Ulaire Attea, Khamul of the East: [raider] Nazgul, cost 6, strength 12, vitality 3, site 3; fierce
					// [wraith] minions that are no Nazgul
					put("predator", "7_196");   // Morgul Predator: [wraith] Orc, cost 5, strength 11, vitality 3, site 4; a skirmish ability used only on purpose
					put("creeper", "8_75");     // Morgul Creeper: [wraith] Orc, cost 3, strength 8, vitality 2, site 4; nothing below 3 burdens
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				"92_20", null
		);
	}

	protected VirtualTableScenario GetShadowScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("enquea", "1_231");     // Úlairë Enquëa, Lieutenant of Morgul: [wraith] Nazgul, cost 6, strength 11, vitality 4, site 3; fierce
					put("attea", "1_229");      // Úlairë Attëa, Keeper of Dol Guldur: [wraith] Nazgul, cost 6, strength 12, vitality 3, site 3; fierce
					put("lemenya", "1_232");    // Úlairë Lemenya, Lieutenant of Morgul: [wraith] Nazgul, cost 4, strength 9, vitality 2, site 3
					put("nelya", "2_84");       // Úlairë Nelya, Ringwraith in Twilight: [wraith] Nazgul, cost 5, vitality 3, site 2; twilight
					put("otsea", "1_235");      // Úlairë Otsëa, Lieutenant of Morgul: [wraith] Nazgul, cost 4, vitality 3, site 3
					put("toldea", "1_236");     // Úlairë Toldëa, Messenger of Morgul: [wraith] Nazgul, cost 6, vitality 3, site 3; fierce
					// The only Nazgul of another culture.  His title is Attëa's: the two are never in play together.
					put("khamul", "103_61");    // Ulaire Attea, Khamul of the East: [raider] Nazgul, cost 6, strength 12, vitality 3, site 3; fierce
					// [wraith] minions that are no Nazgul
					put("predator", "7_196");   // Morgul Predator: [wraith] Orc, cost 5, strength 11, vitality 3, site 4; a skirmish ability used only on purpose
					put("creeper", "8_75");     // Morgul Creeper: [wraith] Orc, cost 3, strength 8, vitality 2, site 4; nothing below 3 burdens
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				null, "92_20"
		);
	}

	@Test
	public void StatsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {
		/**
		 * Set: RTMD 92
		 * Name: Race Text 92_20
		 * Type: MetaSite
		 * Intensity: -1
		 * Game Text: Shadow: Exert a Nazgul twice to play a Nazgul from your discard pile.
		 */

		var scn = GetShadowScenario();

		var card = scn.GetShadowCard("mod");

		assertEquals("Race Text 92_20", card.getBlueprint().getTitle());
		assertEquals(CardType.METASITE, card.getBlueprint().getCardType());
		assertEquals(-1, card.getBlueprint().getIntensity());
	}

	@Test
	public void ExertNazgulTwiceToPlayNazgulFromDiscard() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetShadowScenario();

		var enquea = scn.GetShadowCard("enquea");
		var attea = scn.GetShadowCard("attea");

		scn.MoveMinionsToTable(enquea);
		scn.MoveCardsToDiscard(attea);

		scn.StartGame();
		scn.SetTwilight(20);
		scn.FreepsPass();
		scn.SkipToPhase(Phase.SHADOW);

		var mod = scn.GetShadowCard("mod");
		// 20 set + 1 companion + 2 for site 2
		assertEquals(23, scn.GetTwilight());
		assertEquals(0, scn.GetWoundsOn(enquea));
		assertInZone(Zone.DISCARD, attea);
		assertTrue(scn.ShadowActionAvailable(mod));
		scn.ShadowUseCardAction(mod);

		// One Nazgul to exert and one to play: neither is asked for.
		assertEquals(2, scn.GetWoundsOn(enquea));
		assertInZone(Zone.SHADOW_CHARACTERS, attea);
		// 23 - 6 to play - 2 roaming (site number 3 at site 2)
		assertEquals(15, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AbilityNotAvailableIfNazgulCantExertTwice() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetShadowScenario();

		var lemenya = scn.GetShadowCard("lemenya");
		var attea = scn.GetShadowCard("attea");

		// Lemenya has vitality 2 — exerting twice not available
		scn.MoveMinionsToTable(lemenya);
		scn.MoveCardsToDiscard(attea);

		scn.StartGame();
		scn.SetTwilight(20);
		scn.FreepsPass();
		scn.SkipToPhase(Phase.SHADOW);

		var mod = scn.GetShadowCard("mod");
		// A Nazgul to play and the twilight to play it, and the only Nazgul in play can exert once and no more.
		assertEquals(0, scn.GetWoundsOn(lemenya));
		assertEquals(2, scn.GetVitality(lemenya));
		assertInZone(Zone.DISCARD, attea);
		assertEquals(23, scn.GetTwilight());
		assertFalse(scn.ShadowActionAvailable(mod));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AbilityNotAvailableIfCantAffordNazgulInDiscard() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetShadowScenario();

		var enquea = scn.GetShadowCard("enquea");
		var attea = scn.GetShadowCard("attea");

		scn.MoveMinionsToTable(enquea);
		scn.MoveCardsToDiscard(attea);

		scn.StartGame();
		scn.SetTwilight(0);
		scn.FreepsPass();
		scn.SkipToPhase(Phase.SHADOW);

		var mod = scn.GetShadowCard("mod");
		// 0 set + 1 companion + 2 for site 2: short of Attëa's 6 to play (and 2 roaming).  Enquëa could exert twice.
		assertEquals(3, scn.GetTwilight());
		assertEquals(0, scn.GetWoundsOn(enquea));
		assertInZone(Zone.DISCARD, attea);
		assertFalse(scn.ShadowActionAvailable(mod));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AbilityNotAvailableIfNoNazgulInDiscard() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetShadowScenario();

		var enquea = scn.GetShadowCard("enquea");

		scn.MoveMinionsToTable(enquea);
		// No nazgul in discard

		scn.StartGame();
		scn.SetTwilight(20);
		scn.FreepsPass();
		scn.SkipToPhase(Phase.SHADOW);

		var mod = scn.GetShadowCard("mod");
		assertEquals(0, scn.GetWoundsOn(enquea));
		assertEquals(0, scn.GetShadowDiscardCount());
		assertFalse(scn.ShadowActionAvailable(mod));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void FreepsCannotUseAbility() throws DecisionResultInvalidException, CardNotFoundException {
		// When Freeps owns the modifier, the OwnerIsShadow requirement prevents use
		var scn = GetFreepsScenario();

		var enquea = scn.GetShadowCard("enquea");
		var attea = scn.GetShadowCard("attea");

		scn.MoveMinionsToTable(enquea);
		scn.MoveCardsToDiscard(attea);

		scn.StartGame();
		scn.SetTwilight(20);
		scn.FreepsPass();
		scn.SkipToPhase(Phase.SHADOW);

		var mod = scn.GetFreepsCard("mod");
		// "Your discard pile": the Shadow player has a Nazgul able to exert twice, a Nazgul in his discard pile
		// and the twilight to play it, and the modifier is not his.
		assertEquals(0, scn.GetWoundsOn(enquea));
		assertInZone(Zone.DISCARD, attea);
		assertEquals(23, scn.GetTwilight());
		assertFalse(scn.ShadowActionAvailable(mod));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AnyNazgulAbleToExertTwiceMayPayAndAnyNazgulInYourDiscardPileMayBePlayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetShadowScenario();

		var mod = scn.GetShadowCard("mod");
		var khamul = scn.GetShadowCard("khamul");
		var enquea = scn.GetShadowCard("enquea");
		var predator = scn.GetShadowCard("predator");
		var lemenya = scn.GetShadowCard("lemenya");
		var nelya = scn.GetShadowCard("nelya");
		var creeper = scn.GetShadowCard("creeper");
		var otsea = scn.GetShadowCard("otsea");
		// The other player's Toldëa lies in his own discard pile; the Shadow player's stays in his deck.
		var theirToldea = scn.GetFreepsCard("toldea");

		scn.MoveCardsToDiscard(lemenya, nelya, creeper);
		scn.MoveCardsToDiscard(theirToldea);
		scn.MoveCardsToHand(otsea);

		scn.StartGame();
		// Three minions with 3 or more vitality: a [raider] Nazgul, a [wraith] Nazgul and a [wraith] Orc
		scn.MoveMinionsToTable(khamul, enquea, predator);
		scn.SetTwilight(10);
		scn.FreepsPass();

		// 10 set + 1 companion + 2 for site 2
		assertEquals(13, scn.GetTwilight());
		assertTrue(scn.ShadowActionAvailable(mod));
		scn.ShadowUseCardAction(mod);

		// Either Nazgul, whatever his culture, and not the [wraith] minion who is none
		assertEquals(1, scn.ShadowGetChoiceMin());
		assertTrue(scn.ShadowHasCardChoiceAvailable(khamul, enquea));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(predator));
		assertEquals(2, scn.ShadowGetCardChoiceCount());
		scn.ShadowChooseCard(khamul);
		assertEquals(2, scn.GetWoundsOn(khamul));
		assertEquals(0, scn.GetWoundsOn(enquea));
		assertEquals(0, scn.GetWoundsOn(predator));

		// The two Nazgul in his discard pile and nothing else: not the [wraith] Orc there, not the Nazgul in his
		// hand, and not the Nazgul in the other player's discard pile.
		assertEquals(1, scn.ShadowGetChoiceMin());
		assertTrue(scn.ShadowHasCardChoiceAvailable(lemenya, nelya));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(creeper, otsea, theirToldea));
		assertEquals(2, scn.ShadowGetCardChoiceCount());
		scn.ShadowChooseCards(lemenya);

		assertInZone(Zone.SHADOW_CHARACTERS, lemenya);
		assertInZone(Zone.DISCARD, nelya);
		assertInZone(Zone.DISCARD, creeper);
		assertInZone(Zone.DISCARD, theirToldea);
		assertInZone(Zone.HAND, otsea);
		// 13 - 4 to play - 2 roaming (site number 3 at site 2)
		assertEquals(7, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ANazgulOfAnotherCultureIsPlayedFromTheDiscardPile() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetShadowScenario();

		var mod = scn.GetShadowCard("mod");
		var enquea = scn.GetShadowCard("enquea");
		var khamul = scn.GetShadowCard("khamul");
		var nelya = scn.GetShadowCard("nelya");

		scn.MoveCardsToDiscard(khamul, nelya);

		scn.StartGame();
		scn.MoveMinionsToTable(enquea);
		scn.SetTwilight(10);
		scn.FreepsPass();

		// 10 set + 1 companion + 2 for site 2
		assertEquals(13, scn.GetTwilight());
		assertEquals(0, scn.GetWoundsOn(enquea));
		assertTrue(scn.ShadowActionAvailable(mod));
		scn.ShadowUseCardAction(mod);

		// Enquëa is the only Nazgul in play and exerts unasked.
		assertEquals(2, scn.GetWoundsOn(enquea));

		// A [raider] Nazgul and a [wraith] Nazgul lie in the discard pile: either may be played.
		assertTrue(scn.ShadowHasCardChoiceAvailable(khamul, nelya));
		assertEquals(2, scn.ShadowGetCardChoiceCount());
		scn.ShadowChooseCards(khamul);

		assertInZone(Zone.SHADOW_CHARACTERS, khamul);
		assertInZone(Zone.DISCARD, nelya);
		// 13 - 6 to play - 2 roaming (site number 3 at site 2)
		assertEquals(5, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
