package com.gempukku.lotro.cards.official.set11;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.LotroCardBlueprint;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_11_257_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a [gondor] Man, vitality 4; a maneuver ability never used
					put("legolas", "1_50");     // Legolas, Greenleaf: an archer; his archery ability is never used, so he adds 1 to the fellowship archery total
					// Defend It and Hope: [gondor] Maneuver event, "Exert a [gondor] Man to wound a minion once (or twice
					// if that minion is an Uruk-hai)".
					put("defend", "4_115");

					// The [uruk-hai] minions: an Uruk-hai and a Wizard.
					put("destroyer", "17_124"); // White Hand Destroyer: [uruk-hai] Uruk-hai, vitality 3, fierce; hunter only for sites controlled
					put("saruman", "17_116");   // Saruman, Master of the White Hand: [uruk-hai] Wizard, vitality 4; companions resistance -1, a skirmish ability never used
					// Uruk-hai of other cultures.
					put("guard", "10_81");      // Cirith Ungol Guard: [sauron] Uruk-hai, vitality 2; Damage +1 is its whole text
					put("pursuer", "4_190");    // Uruk Pursuer: [isengard] Uruk-hai, vitality 3, fierce; its texts are about unbound Hobbits
				}},
				new HashMap<>()
				{{
					put("site1", "11_239");     // Fangorn Glade: adds (1) for each Free Peoples weapon when moved to; it is only left here
					put("uplands", "11_257");
					put("ruins", "18_138");     // Sirannon Ruins: River and no text
					// Never laid in these tests.
					put("site4", "17_148");
					put("site5", "11_230");
					put("site6", "13_185");
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
	public void RohanUplandsStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 11
		 * Name: Rohan Uplands
		 * Unique: False
		 * Type: Site
		 * Block: Shadows
		 * Shadow Number: 1
		 * Direction: Right
		 * Game Text: Plains. During the archery phase, [uruk-hai] minions cannot take wounds.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsSite("uplands");

		assertEquals("Rohan Uplands", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(CardType.SITE, card.getBlueprint().getCardType());
		assertEquals(SitesBlock.SHADOWS, card.getBlueprint().getSiteBlock());
		// A Shadows-block site has no site number of its own.
		assertEquals(0, card.getBlueprint().getSiteNumber());
		assertEquals(1, card.getBlueprint().getTwilightCost());
		assertEquals(LotroCardBlueprint.Direction.RIGHT, card.getBlueprint().getSiteDirection());
		assertTrue(scn.HasKeyword(card, Keyword.PLAINS));
	}

	@Test
	public void UrukHaiCultureMinionsCanBeWoundedInTheManeuverPhaseButNotInTheArcheryPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var legolas = scn.GetFreepsCard("legolas");
		var defend = scn.GetFreepsCard("defend");
		var destroyer = scn.GetShadowCard("destroyer");
		var saruman = scn.GetShadowCard("saruman");
		var guard = scn.GetShadowCard("guard");
		var pursuer = scn.GetShadowCard("pursuer");
		scn.MoveCompanionsToTable(aragorn, legolas);
		scn.MoveCardsToHand(defend);

		scn.StartGame(scn.GetFreepsSite("site1"));
		// The Shadow player lays the next site, and the fellowship moves to it.
		scn.FreepsPass();
		var uplands = scn.GetShadowSite("uplands");
		scn.ShadowChooseCardBPFromSelection(uplands);
		assertSame(uplands, scn.GetCurrentSite());
		scn.MoveMinionsToTable(destroyer, saruman, guard, pursuer);

		// The maneuver phase, next door to the archery phase: every minion can be wounded, the [uruk-hai] ones
		// included.
		scn.SkipToPhase(Phase.MANEUVER);
		assertEquals(0, scn.GetWoundsOn(destroyer));
		scn.FreepsPlayCard(defend);
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertTrue(scn.FreepsHasCardChoiceAvailable(destroyer, saruman, guard, pursuer));
		scn.FreepsChooseCard(destroyer);
		// An Uruk-hai: wounded twice.
		assertEquals(2, scn.GetWoundsOn(destroyer));

		// The archery phase: the fellowship's archery wound (Legolas, 1) cannot go on an [uruk-hai] minion.
		scn.SkipToArcheryWounds();
		assertEquals(0, scn.GetWoundsOn(saruman));
		assertEquals(0, scn.GetWoundsOn(guard));
		assertEquals(0, scn.GetWoundsOn(pursuer));
		assertTrue(scn.ShadowHasCardChoiceAvailable(guard, pursuer));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(destroyer, saruman));
		scn.ShadowChooseCard(guard);

		assertEquals(1, scn.GetWoundsOn(guard));
		assertEquals(0, scn.GetWoundsOn(pursuer));
		assertEquals(2, scn.GetWoundsOn(destroyer));
		assertEquals(0, scn.GetWoundsOn(saruman));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void OnceTheFellowshipHasLeftTheUplandsAnUrukHaiCultureMinionTakesAnArcheryWound() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var legolas = scn.GetFreepsCard("legolas");
		var destroyer = scn.GetShadowCard("destroyer");
		var saruman = scn.GetShadowCard("saruman");
		var guard = scn.GetShadowCard("guard");
		scn.MoveCompanionsToTable(legolas);

		scn.StartGame(scn.GetFreepsSite("site1"));
		scn.FreepsPass();
		var uplands = scn.GetShadowSite("uplands");
		scn.ShadowChooseCardBPFromSelection(uplands);
		assertSame(uplands, scn.GetCurrentSite());

		// No minion: on to the regroup phase, and a second move, to a site with no text.
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();
		var ruins = scn.GetShadowSite("ruins");
		scn.ShadowChooseCardBPFromSelection(ruins);
		assertSame(ruins, scn.GetCurrentSite());
		scn.MoveMinionsToTable(destroyer, saruman, guard);

		scn.SkipToArcheryWounds();
		assertEquals(0, scn.GetWoundsOn(destroyer));
		assertTrue(scn.ShadowHasCardChoiceAvailable(destroyer, saruman, guard));
		scn.ShadowChooseCard(destroyer);

		assertEquals(1, scn.GetWoundsOn(destroyer));
		assertEquals(0, scn.GetWoundsOn(saruman));
		assertEquals(0, scn.GetWoundsOn(guard));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}
}
