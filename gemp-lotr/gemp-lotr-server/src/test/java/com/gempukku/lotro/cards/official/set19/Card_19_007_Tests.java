package com.gempukku.lotro.cards.official.set19;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_19_007_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("legolas", "19_7");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a [gondor] companion; a maneuver ability used only on purpose
					put("bow", "1_90");         // Aragorn's Bow: makes Aragorn an archer, with an Archery ability of its own
					put("runner", "1_178");     // Goblin Runner: a [moria] Orc, vitality 1 (exhausted)
					put("savage", "1_151");     // Uruk Savage: an [isengard] Uruk-hai, vitality 3, Damage +1 and no other text
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void LegolasStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 19
		 * Name: Legolas, Skeptical Guide
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Elven
		 * Twilight Cost: 2
		 * Type: Companion
		 * Subtype: Elf
		 * Strength: 6
		 * Vitality: 3
		 * Resistance: 7
		 * Game Text: <b>Fellowship</b>. <b>Archer</b>.<br><b>Archery:</b> Add two threats to wound a minion; Legolas does not add to the fellowship archery total.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("legolas");

		assertEquals("Legolas", card.getBlueprint().getTitle());
		assertEquals("Skeptical Guide", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ELVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.ELF, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.FELLOWSHIP));
		assertTrue(scn.HasKeyword(card, Keyword.ARCHER));
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(6, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(7, card.getBlueprint().getResistance());
	}

	@Test
	public void ArcheryAbilityAddsTwoThreatsToWoundAMinionAndHeAddsNothingToTheArcheryTotalForThePhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var legolas = scn.GetFreepsCard("legolas");
		var aragorn = scn.GetFreepsCard("aragorn");
		var bow = scn.GetFreepsCard("bow");
		var frodo = scn.GetRingBearer();
		var runner = scn.GetShadowCard("runner");
		var savage = scn.GetShadowCard("savage");
		// Three companions: room for two threats.
		scn.MoveCompanionsToTable(legolas, aragorn);
		scn.AttachCardsTo(aragorn, bow);

		scn.StartGame();
		scn.MoveMinionsToTable(runner, savage);
		scn.SkipToPhase(Phase.ARCHERY);

		// Legolas and Aragorn, an archer by the Bow, each add 1.
		assertEquals(2, scn.GetFreepsArcheryTotal());
		assertEquals(0, scn.GetThreats());
		assertEquals(0, scn.GetWoundsOn(legolas));
		assertTrue(scn.FreepsActionAvailable(legolas));

		scn.FreepsUseCardAction(legolas);
		assertEquals(2, scn.GetThreats());
		assertEquals(0, scn.GetWoundsOn(legolas));
		assertTrue(scn.FreepsHasCardChoiceAvailable(runner, savage));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(legolas, aragorn, frodo));
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertEquals(1, scn.FreepsGetChoiceMax());

		// The Runner, of vitality 1, is wounded and so killed: a wound, not an exertion.
		scn.FreepsChooseCard(runner);
		assertInZone(Zone.DISCARD, runner);
		assertEquals(0, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		// Legolas no longer adds; Aragorn still does.
		assertEquals(1, scn.GetFreepsArcheryTotal());
		assertTrue(scn.AwaitingShadowArcheryPhaseActions());

		// The one archery wound of the fellowship goes on the Uruk-hai, the one minion left.
		scn.SkipToArcheryWounds();
		assertEquals(1, scn.GetWoundsOn(savage));

		// Once the archery phase is over, Legolas counts again.
		assertEquals(2, scn.GetFreepsArcheryTotal());
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void ArcheryAbilityIsNotOfferedWhenTwoThreatsCannotBeAdded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var legolas = scn.GetFreepsCard("legolas");
		var aragorn = scn.GetFreepsCard("aragorn");
		var bow = scn.GetFreepsCard("bow");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCompanionsToTable(legolas, aragorn);
		scn.AttachCardsTo(aragorn, bow);

		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		// Three companions and two threats: one more could be added, two cannot.
		scn.AddThreats(2);
		assertEquals(2, scn.GetThreats());
		scn.SkipToPhase(Phase.ARCHERY);

		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
		assertFalse(scn.FreepsActionAvailable(legolas));
		// The Bow's ability, with Aragorn able to exert, is offered beside it.
		assertTrue(scn.FreepsActionAvailable(bow));
		assertEquals(2, scn.GetThreats());
	}
}
