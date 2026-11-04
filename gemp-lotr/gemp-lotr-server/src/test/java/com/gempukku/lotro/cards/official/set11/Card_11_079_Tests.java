package com.gempukku.lotro.cards.official.set11;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_11_079_Tests
{

	// The Towers path: site 2 (Eastfold) is a plains and nothing more, site 3 (Westemnet Plains) a plains and a
	// sanctuary, site 4 (White Rocks) a battleground and no plains.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("dunlending1", "11_79");
					put("dunlending2", "11_79");
					// Corps of Harad: a [men] Man minion; fierce at a battleground, and no battleground is laid where it is played
					put("corps", "11_73");

					put("sam", "1_311");        // Sam, Son of Hamfast: Ring-bound, not the Ring-bearer; abilities used only on purpose
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: an unbound [gondor] Man, resistance 6
					put("gimli", "1_13");       // Gimli, Son of Gloin: an unbound [dwarven] Dwarf, resistance 6
					put("thrarin", "1_27");     // Thrarin, Dwarven Smith: an ally, no companion
				}},
				new HashMap<>()
				{{
					putAll(VirtualTableScenario.TowersSites);
					// Westemnet Plains: Plains. Sanctuary. Its bar on [dunland] Men's archery wounds reaches no test.
					put("site3", "4_342");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void FearsomeDunlendingStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 11
		 * Name: Fearsome Dunlending
		 * Unique: False
		 * Side: Shadow
		 * Culture: Men
		 * Twilight Cost: 3
		 * Type: Minion
		 * Subtype: Man
		 * Strength: 9
		 * Vitality: 2
		 * Site Number: 4
		 * Game Text: <b>Ambush (1)</b>.<br>When you play this minion at a plains site, you may spot an unbound companion to make that companion resistance -1 until the end of the turn.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("dunlending1");

		assertEquals("Fearsome Dunlending", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.MEN, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.AMBUSH));
		assertEquals(1, scn.GetKeywordCount(card, Keyword.AMBUSH));
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(9, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void PlayedAtAPlainsItMakesAnUnboundCompanionResistanceMinus1UntilTheEndOfTheTurn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var dunlending1 = scn.GetShadowCard("dunlending1");
		var dunlending2 = scn.GetShadowCard("dunlending2");
		var frodo = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var thrarin = scn.GetFreepsCard("thrarin");
		scn.MoveCompanionsToTable(sam, aragorn, gimli);
		scn.MoveAlliesToTable(thrarin);
		scn.MoveCardsToHand(dunlending2);

		scn.StartGame();
		// The other copy is laid early: its own trigger answers only its own play.
		scn.MoveMinionsToTable(dunlending1);
		scn.SetTwilight(10);

		// The fellowship moves to Eastfold.
		scn.FreepsPass();
		assertTrue(scn.HasKeyword(scn.GetCurrentSite(), Keyword.PLAINS));
		assertEquals(10, scn.GetResistance(frodo));
		assertEquals(5, scn.GetResistance(sam));
		assertEquals(6, scn.GetResistance(aragorn));
		assertEquals(6, scn.GetResistance(gimli));

		scn.ShadowPlayCard(dunlending2);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(dunlending2));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(dunlending1));
		scn.ShadowAcceptOptionalTrigger();

		// The unbound companions, of two cultures and races; not a Ring-bound companion, the Ring-bearer or an ally.
		assertTrue(scn.ShadowHasCardChoiceAvailable(aragorn, gimli));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(frodo, sam, thrarin));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(aragorn);

		assertEquals(5, scn.GetResistance(aragorn));
		assertEquals(6, scn.GetResistance(gimli));
		assertEquals(5, scn.GetResistance(sam));
		assertEquals(10, scn.GetResistance(frodo));
		assertTrue(scn.AwaitingShadowPhaseActions());

		// Still in force in the Shadow phase after a second move, to Westemnet Plains.
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertEquals(5, scn.GetResistance(aragorn));

		// Gone by the Free Peoples player's next turn.
		scn.SkipToOtherPlayersTurn();
		scn.SkipToOtherPlayersTurn();
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertEquals(6, scn.GetResistance(aragorn));
	}

	@Test
	public void DecliningTheOfferChangesNoResistance() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var dunlending = scn.GetShadowCard("dunlending1");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(aragorn, gimli);
		scn.MoveCardsToHand(dunlending);

		scn.StartGame();
		scn.SetTwilight(10);
		scn.FreepsPass();
		assertEquals(6, scn.GetResistance(aragorn));
		assertEquals(6, scn.GetResistance(gimli));

		scn.ShadowPlayCard(dunlending);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(dunlending));
		scn.ShadowDeclineOptionalTrigger();

		assertEquals(6, scn.GetResistance(aragorn));
		assertEquals(6, scn.GetResistance(gimli));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void PlayedAtAPlainsThatIsASanctuaryItIsOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var dunlending = scn.GetShadowCard("dunlending1");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(dunlending);

		scn.StartGame();
		scn.SkipToSite(2);
		scn.SetTwilight(10);

		// The fellowship moves to Westemnet Plains.
		scn.FreepsPass();
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertTrue(scn.HasKeyword(scn.GetCurrentSite(), Keyword.SANCTUARY));
		assertEquals(6, scn.GetResistance(aragorn));

		scn.ShadowPlayCard(dunlending);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(dunlending));
		// Aragorn is the one unbound companion, and is chosen unasked.
		scn.ShadowAcceptOptionalTrigger();

		assertEquals(5, scn.GetResistance(aragorn));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void PlayedAwayFromAPlainsItOffersNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var dunlending = scn.GetShadowCard("dunlending1");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(dunlending);

		scn.StartGame();
		scn.SkipToSite(3);
		scn.SetTwilight(10);

		// The fellowship moves to White Rocks, a battleground and no plains.
		scn.FreepsPass();
		assertEquals(4, scn.GetCurrentSiteNumber());
		assertFalse(scn.HasKeyword(scn.GetCurrentSite(), Keyword.PLAINS));
		assertEquals(6, scn.GetResistance(aragorn));

		scn.ShadowPlayCard(dunlending);
		assertInZone(Zone.SHADOW_CHARACTERS, dunlending);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(dunlending));
		assertEquals(6, scn.GetResistance(aragorn));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AnotherMenMinionPlayedAtThePlainsOffersNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var dunlending = scn.GetShadowCard("dunlending1");
		var corps = scn.GetShadowCard("corps");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(corps);

		scn.StartGame();
		scn.MoveMinionsToTable(dunlending);
		scn.SetTwilight(10);
		scn.FreepsPass();
		assertTrue(scn.HasKeyword(scn.GetCurrentSite(), Keyword.PLAINS));
		assertEquals(6, scn.GetResistance(aragorn));

		scn.ShadowPlayCard(corps);
		assertInZone(Zone.SHADOW_CHARACTERS, corps);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(dunlending));
		assertEquals(6, scn.GetResistance(aragorn));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithNoUnboundCompanionToSpotItOffersNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var dunlending = scn.GetShadowCard("dunlending1");
		var frodo = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		var thrarin = scn.GetFreepsCard("thrarin");
		scn.MoveCompanionsToTable(sam);
		scn.MoveAlliesToTable(thrarin);
		scn.MoveCardsToHand(dunlending);

		scn.StartGame();
		scn.SetTwilight(10);
		scn.FreepsPass();
		assertTrue(scn.HasKeyword(scn.GetCurrentSite(), Keyword.PLAINS));
		assertEquals(10, scn.GetResistance(frodo));
		assertEquals(5, scn.GetResistance(sam));

		scn.ShadowPlayCard(dunlending);
		assertInZone(Zone.SHADOW_CHARACTERS, dunlending);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(dunlending));
		assertEquals(10, scn.GetResistance(frodo));
		assertEquals(5, scn.GetResistance(sam));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
