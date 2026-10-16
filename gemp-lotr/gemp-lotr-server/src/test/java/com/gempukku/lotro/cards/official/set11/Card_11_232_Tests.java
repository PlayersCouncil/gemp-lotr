package com.gempukku.lotro.cards.official.set11;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.LotroCardBlueprint;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_11_232_Tests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("gimli", "1_13");       // Gimli, Son of Glóin: companion, strength 6, vitality 3; "Skirmish: Exert Gimli to make him strength +2."
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: companion, vitality 4; "Maneuver: Exert Aragorn to make him defender +1 ..."
					put("halls", "1_26");       // Their Halls of Stone: Skirmish event, cost 0, "Make a Dwarf strength +2 (or +4 if at an underground site)."

					put("slayer", "1_153");     // Uruk Slayer: minion, strength 9, vitality 2; "Skirmish: Remove (1) to make this minion strength +1 (limit +3)."
					put("fear", "1_278");       // Strength Born of Fear: support-area condition; "Skirmish: Remove (3) to make a [sauron] Orc strength +1."
				}},
				new HashMap<>()
				{{
					put("site1", "11_239");     // Fangorn Glade: adds (1) for each Free Peoples weapon when moved to; there are none
					put("cavern", "11_232");
					put("site3", "11_234");     // Crags of Emyn Muil: wounds on a regroup move from it in region 2 only
					// Never laid in these tests.
					put("site4", "17_148");
					put("site5", "18_138");
					put("site6", "11_230");
					put("site7", "12_187");
					put("site8", "12_185");
					put("site9", "17_146");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				VirtualTableScenario.Shadows
		);
	}

	@Test
	public void CavernEntranceStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 11
		 * Name: Cavern Entrance
		 * Unique: false
		 * Type: Site
		 * Block: Shadows
		 * Shadow Number: 1
		 * Direction: Right
		 * Game Text: <b>Underground</b>. Skirmish special abilities cannot be used.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsSite("cavern");

		assertEquals("Cavern Entrance", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(CardType.SITE, card.getBlueprint().getCardType());
		assertEquals(SitesBlock.SHADOWS, card.getBlueprint().getSiteBlock());
		// A Shadows-block site has no site number of its own.
		assertEquals(0, card.getBlueprint().getSiteNumber());
		assertEquals(1, card.getBlueprint().getTwilightCost());
		assertEquals(LotroCardBlueprint.Direction.RIGHT, card.getBlueprint().getSiteDirection());
		assertTrue(scn.HasKeyword(card, Keyword.UNDERGROUND));
	}

	@Test
	public void AtCavernEntranceNoSkirmishSpecialAbilityOfEitherPlayerCanBeUsedButASkirmishEventAndAManeuverAbilityCan() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gimli = scn.GetFreepsCard("gimli");
		var aragorn = scn.GetFreepsCard("aragorn");
		var halls = scn.GetFreepsCard("halls");
		var slayer = scn.GetShadowCard("slayer");
		var fear = scn.GetShadowCard("fear");
		scn.MoveCompanionsToTable(gimli, aragorn);
		scn.MoveCardsToHand(halls);
		scn.MoveCardsToSupportArea(fear);

		scn.StartGame(scn.GetFreepsSite("site1"));
		scn.FreepsPass();
		// The Shadow player lays Cavern Entrance as site 2.
		scn.ShadowChooseCardBPFromSelection(scn.GetShadowSite("cavern"));
		assertSame(scn.GetShadowSite("cavern"), scn.GetCurrentSite());

		scn.MoveMinionsToTable(slayer);
		scn.SetTwilight(10);
		scn.SkipToPhase(Phase.MANEUVER);

		// A special ability of another phase is not barred.
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertTrue(scn.FreepsActionAvailable(aragorn));

		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.PassCurrentPhaseActions();
		scn.FreepsAssignToMinions(gimli, slayer);
		scn.FreepsResolveSkirmish(gimli);

		// Gimli could exert, but his special ability cannot be used. A Skirmish event is not a special ability,
		// and Cavern Entrance is underground: 6 + 4.
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertFalse(scn.FreepsActionAvailable(gimli));
		assertTrue(scn.FreepsPlayAvailable(halls));
		assertEquals(6, scn.GetStrength(gimli));
		scn.FreepsPlayCard(halls);
		assertEquals(10, scn.GetStrength(gimli));
		assertInDiscard(halls);

		// Nor can the Shadow player's, a minion's or a condition's: the pool holds what each would cost.
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertEquals(10, scn.GetTwilight());
		assertFalse(scn.ShadowActionAvailable(slayer));
		assertFalse(scn.ShadowActionAvailable(fear));
	}

	@Test
	public void OnceTheFellowshipHasMovedOnFromCavernEntranceSkirmishSpecialAbilitiesCanBeUsed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gimli = scn.GetFreepsCard("gimli");
		var slayer = scn.GetShadowCard("slayer");
		var fear = scn.GetShadowCard("fear");
		scn.MoveCompanionsToTable(gimli);
		scn.MoveCardsToSupportArea(fear);

		scn.StartGame(scn.GetFreepsSite("site1"));
		scn.FreepsPass();
		scn.ShadowChooseCardBPFromSelection(scn.GetShadowSite("cavern"));
		assertSame(scn.GetShadowSite("cavern"), scn.GetCurrentSite());

		// The regroup move takes the fellowship on to site 3, with Cavern Entrance behind it.
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();
		scn.ShadowChooseCardBPFromSelection(scn.GetShadowSite("site3"));
		assertSame(scn.GetShadowSite("site3"), scn.GetCurrentSite());
		assertEquals(3, scn.GetCurrentSiteNumber());

		scn.MoveMinionsToTable(slayer);
		scn.SetTwilight(10);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.PassCurrentPhaseActions();
		scn.FreepsAssignToMinions(gimli, slayer);
		scn.FreepsResolveSkirmish(gimli);

		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertTrue(scn.FreepsActionAvailable(gimli));
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertTrue(scn.ShadowActionAvailable(slayer));
		assertTrue(scn.ShadowActionAvailable(fear));
	}
}
