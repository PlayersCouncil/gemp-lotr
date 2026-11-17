package com.gempukku.lotro.cards.official.set04;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.common.Timeword;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_04_005_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("village", "4_5");
					put("brigand", "4_10");     // Dunlending Brigand: [dunland] Man, strength 8, vitality 1, no text
					put("savage", "4_17");      // Dunlending Savage: [dunland] Man, strength 11, vitality 1, no text
					put("axeman", "4_224");     // Easterling Axeman: a [raider] Man, strength 6, vitality 2; stronger and fierce only at 2 burdens
					put("saruman", "4_33");     // Saruman, Rabble-rouser: a [dunland] Wizard, strength 8; may not be assigned to a skirmish;
					                            // a skirmish ability used only on purpose

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a [gondor] Man, strength 8, vitality 4; a maneuver ability used only on purpose
					put("gimli", "1_13");       // Gimli, Son of Gloin: strength 6, vitality 3; a skirmish ability used only on purpose
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void BurnEveryVillageStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Burn Every Village
		 * Unique: False
		 * Side: Shadow
		 * Culture: Dunland
		 * Twilight Cost: 0
		 * Type: Event
		 * Subtype: Skirmish
		 * Game Text: <b>Skirmish:</b> Make a [dunland] Man strength +2 (or +4 if you control a site).
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("village");

		assertEquals("Burn Every Village", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.DUNLAND, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
        assertTrue(scn.HasTimeword(card, Timeword.SKIRMISH));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void MakesADunlandManStrengthPlus2UntilTheSkirmishIsOverWhenOnlyTheOtherPlayerControlsASite() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var village = scn.GetShadowCard("village");
		var brigand = scn.GetShadowCard("brigand");
		var savage = scn.GetShadowCard("savage");
		var axeman = scn.GetShadowCard("axeman");
		var saruman = scn.GetShadowCard("saruman");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(aragorn, gimli);

		scn.StartGame();
		scn.SkipToSite(2);
		// Player 1 took site 1 on player 2's turn: a site is controlled, but not by the event's player.
		scn.FreepsTakeControlOfSite();
		scn.MoveCardsToHand(village);
		// Laid after the move, so that Saruman's own move trigger has nothing to count.
		scn.FreepsPass();
		scn.MoveMinionsToTable(brigand, savage, axeman, saruman);
		assertEquals(1, scn.GetFreepsControlledSiteCount());
		assertEquals(0, scn.GetShadowControlledSiteCount());

		// The phase next door: the Shadow player's assignment actions, with [dunland] Men in play.
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(village));
		scn.ShadowPass();

		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, brigand}, new PhysicalCardImpl[]{gimli, axeman});
		// The Savage fights nobody.
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertEquals(8, scn.GetStrength(brigand));
		assertEquals(11, scn.GetStrength(savage));
		assertTrue(scn.ShadowPlayAvailable(village));
		scn.ShadowPlayCard(village);
		// A [dunland] Man in the skirmish, and one who is in none.
		assertTrue(scn.ShadowHasCardChoiceAvailable(brigand, savage));
		// A [dunland] minion who is no Man, a Man of another culture, and a [gondor] Man companion.
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(saruman, axeman, aragorn, gimli));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(brigand);

		assertInZone(Zone.DISCARD, village);
		// 8 + 2
		assertEquals(10, scn.GetStrength(brigand));
		assertEquals(11, scn.GetStrength(savage));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());

		// 10 against 8: the Brigand wins, and the bonus holds to the end of his skirmish.
		scn.FreepsPass();
		scn.ShadowPass();
		assertEquals(1, scn.GetWoundsOn(aragorn));

		// The next skirmish is a skirmish phase of its own: the bonus is gone.
		scn.FreepsResolveSkirmish(gimli);
		assertEquals(8, scn.GetStrength(brigand));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void MakesADunlandManStrengthPlus4InsteadWhileYouControlASite() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var village = scn.GetShadowCard("village");
		var brigand = scn.GetShadowCard("brigand");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.SkipToSite(2);
		scn.ShadowTakeControlOfSite();
		scn.MoveCardsToHand(village);
		scn.FreepsPass();
		scn.MoveMinionsToTable(brigand);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, brigand);
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertEquals(1, scn.GetShadowControlledSiteCount());
		assertEquals(8, scn.GetStrength(brigand));
		scn.ShadowPlayCard(village);

		// 8 + 4, with no choice between the two bonuses asked: not 8 + 2, nor 8 + 2 + 4.
		assertInZone(Zone.DISCARD, village);
		assertEquals(12, scn.GetStrength(brigand));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}
}
