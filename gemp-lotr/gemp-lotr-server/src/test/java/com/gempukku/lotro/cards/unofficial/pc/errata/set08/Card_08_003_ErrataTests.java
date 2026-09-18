package com.gempukku.lotro.cards.unofficial.pc.errata.set08;

import com.gempukku.lotro.framework.*;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;
import static com.gempukku.lotro.framework.Assertions.*;

public class Card_08_003_ErrataTests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("brc", "58_3");
					put("gimli", "1_13");      // Damage +1
					put("axe", "1_14");        // Gimli's Battle Axe: bearer is damage +1
					put("durin", "9_3");       // Durin III: Damage +1
					put("guard", "1_7");       // Dwarf Guard: no damage bonus
					put("troop1", "1_177");
					put("troop2", "1_177");
					put("troop3", "1_177");
					put("watcher1", "103_92"); // Cirith Ungol Watcher: response hinders a companion exerted by a FP card
					put("watcher2", "103_92");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void BloodRunsChillStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 8
		 * Name: Blood Runs Chill
		 * Unique: false
		 * Side: Free Peoples
		 * Culture: Dwarven
		 * Twilight Cost: 2
		 * Type: Event
		 * Subtype: Response
		 * Game Text: If the fellowship moves, spot a Dwarf who is damage +X and exert that Dwarf to make an opponent hinder X Shadow cards.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("brc");

		assertEquals("Blood Runs Chill", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.DWARVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.RESPONSE));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	private static void ShadowPlaysSiteIfAsked(VirtualTableScenario scn) throws DecisionResultInvalidException {
		if (scn.ShadowDecisionAvailable("Choose site to play"))
			scn.ShadowChooseAnyCard();
	}

	private static int CountHindered(VirtualTableScenario scn, com.gempukku.lotro.game.PhysicalCardImpl... cards) {
		int n = 0;
		for (var card : cards)
			if (scn.IsHindered(card))
				n++;
		return n;
	}

	@Test
	public void ExertsADamageDwarfToMakeShadowHinderThatManyCards() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var brc = scn.GetFreepsCard("brc");
		var gimli = scn.GetFreepsCard("gimli");
		var axe = scn.GetFreepsCard("axe");
		var durin = scn.GetFreepsCard("durin");
		var guard = scn.GetFreepsCard("guard");
		scn.MoveCompanionsToTable(gimli, durin, guard);
		scn.AttachCardsTo(gimli, axe);
		scn.MoveCardsToHand(brc);

		var troop1 = scn.GetShadowCard("troop1");
		var troop2 = scn.GetShadowCard("troop2");
		var troop3 = scn.GetShadowCard("troop3");
		scn.MoveMinionsToTable(troop1, troop2, troop3);

		scn.StartGame();

		assertEquals(2, scn.GetKeywordCount(gimli, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(durin, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(guard, Keyword.DAMAGE));

		scn.FreepsPassCurrentPhaseAction();          // the fellowship moves
		ShadowPlaysSiteIfAsked(scn);

		assertTrue(scn.FreepsHasOptionalTriggerAvailable());
		scn.FreepsAcceptOptionalTrigger();

		// Only Dwarves with a damage bonus can be chosen
		assertTrue(scn.FreepsHasCardChoiceAvailable(gimli, durin));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(guard));
		scn.FreepsChooseCard(gimli);
		assertEquals(1, scn.GetWoundsOn(gimli));

		// Gimli is damage +2, so the Shadow player must hinder 2 of the 3 minions
		assertTrue(scn.ShadowHasCardChoiceAvailable(troop1, troop2, troop3));
		scn.ShadowChooseCards(troop1, troop2);

		assertTrue(scn.IsHindered(troop1));
		assertTrue(scn.IsHindered(troop2));
		assertFalse(scn.IsHindered(troop3));
		assertEquals(Zone.DISCARD, brc.getZone());
	}

	@Test
	public void DamageBonusIsLockedInWhenTheDwarfIsExerted() throws DecisionResultInvalidException, CardNotFoundException {
		// Regression for #1026: X was evaluated when the effect resolved. A Cirith Ungol Watcher responding to the
		// exertion hindered Gimli first, dropping his bonus to the printed +1, so Shadow only had to hinder 1 card.
		var scn = GetScenario();

		var brc = scn.GetFreepsCard("brc");
		var gimli = scn.GetFreepsCard("gimli");
		var axe = scn.GetFreepsCard("axe");
		scn.MoveCompanionsToTable(gimli);
		scn.AttachCardsTo(gimli, axe);
		scn.MoveCardsToHand(brc);

		var troop1 = scn.GetShadowCard("troop1");
		var troop2 = scn.GetShadowCard("troop2");
		var troop3 = scn.GetShadowCard("troop3");
		var watcher1 = scn.GetShadowCard("watcher1");
		var watcher2 = scn.GetShadowCard("watcher2");
		scn.MoveMinionsToTable(troop1, troop2, troop3);
		scn.MoveCardsToSupportArea(watcher1, watcher2);

		scn.StartGame();
		assertEquals(2, scn.GetKeywordCount(gimli, Keyword.DAMAGE));

		scn.FreepsPassCurrentPhaseAction();
		ShadowPlaysSiteIfAsked(scn);

		assertTrue(scn.FreepsHasOptionalTriggerAvailable());
		scn.FreepsAcceptOptionalTrigger();           // Gimli is the only eligible Dwarf: chosen and exerted
		assertEquals(1, scn.GetWoundsOn(gimli));

		// The Watcher responds to the exertion: discard the other Watcher, hinder Gimli
		assertTrue(scn.ShadowHasOptionalTriggerAvailable());
		scn.ShadowAcceptOptionalTrigger();
		if (scn.ShadowHasCardChoiceAvailable(watcher2))
			scn.ShadowChooseCard(watcher2);
		assertTrue(scn.IsHindered(gimli));
		assertEquals(1, scn.GetKeywordCount(gimli, Keyword.DAMAGE));   // the axe is inactive now

		// ...but X was locked in at +2 when the cost was paid
		assertTrue(scn.ShadowHasCardChoiceAvailable(troop1, troop2, troop3));
		scn.ShadowChooseCards(troop1, troop2);
		assertEquals(2, CountHindered(scn, troop1, troop2, troop3));
	}
}
