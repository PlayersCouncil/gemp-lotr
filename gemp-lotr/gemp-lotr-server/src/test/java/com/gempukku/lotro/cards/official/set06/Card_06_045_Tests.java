package com.gempukku.lotro.cards.official.set06;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;
import static com.gempukku.lotro.framework.Assertions.*;

public class Card_06_045_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("smeagol", "6_45");
					put("sam", "1_311");        // Sam, Son of Hamfast: a companion in another skirmish; his abilities are used only on purpose
					// The cards of the Free Peoples hand: a companion, a possession, an event, and a Shadow card of his deck
					put("aragorn", "1_89");     // Aragorn, Ranger of the North
					put("brand", "2_32");       // Flaming Brand
					put("sleep", "1_84");       // Sleep, Caradhras
					put("troop", "1_177");      // Goblin Patrol Troop

					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai, strength 5, vitality 3; damage +1 and nothing else
					put("runner", "1_178");     // Goblin Runner: [moria] Orc, strength 5, vitality 1; its play trigger is not met when cheated in
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void SmeagolStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 6
		 * Name: Sméagol, Poor Creature
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Gollum
		 * Twilight Cost: 0
		 * Type: Companion
		 * Race: (none)
		 * Strength: 3
		 * Vitality: 4
		 * Resistance: 6
		 * Signet: Frodo
		 * Game Text: <b>Ring-bound</b>. To play, add a burden.<br><b>Skirmish:</b> Discard a card from hand to make Sméagol strength +1.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("smeagol");

		assertEquals("Sméagol", card.getBlueprint().getTitle());
		assertEquals("Poor Creature", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GOLLUM, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		// CR 4.2 "Gollum/Sméagol": he has no race.
		assertNull(card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.RING_BOUND));
		assertEquals(0, card.getBlueprint().getTwilightCost());
		assertEquals(3, card.getBlueprint().getStrength());
		assertEquals(4, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getResistance());
		assertEquals(Signet.FRODO, card.getBlueprint().getSignet());
	}

	@Test
	public void PlayingSmeagolAddsABurden() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var smeagol = scn.GetFreepsCard("smeagol");
		scn.MoveCardsToHand(smeagol);

		scn.StartGame();
		assertInHand(smeagol);
		assertEquals(0, scn.GetBurdens());
		assertTrue(scn.FreepsPlayAvailable(smeagol));
		scn.FreepsPlayCard(smeagol);

		assertInZone(Zone.FREE_CHARACTERS, smeagol);
		assertEquals(1, scn.GetBurdens());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void SkirmishAbilityDiscardsACardOfAnyKindFromHandToMakeHimStrengthPlusOne() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var smeagol = scn.GetFreepsCard("smeagol");
		var sam = scn.GetFreepsCard("sam");
		var aragorn = scn.GetFreepsCard("aragorn");
		var brand = scn.GetFreepsCard("brand");
		var sleep = scn.GetFreepsCard("sleep");
		var troop = scn.GetFreepsCard("troop");
		scn.MoveCompanionsToTable(smeagol, sam);

		var savage = scn.GetShadowCard("savage");
		var runner = scn.GetShadowCard("runner");

		scn.StartGame();
		scn.MoveMinionsToTable(savage, runner);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{smeagol, savage}, new PhysicalCardImpl[]{sam, runner});
		scn.SetFreepsHand(aragorn, brand, sleep, troop);
		scn.FreepsResolveSkirmish(smeagol);

		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(4, scn.GetFreepsHandCount());
		assertEquals(3, scn.GetStrength(smeagol));
		assertEquals(3, scn.GetStrength(sam));
		assertTrue(scn.FreepsActionAvailable(smeagol));
		scn.FreepsUseCardAction(smeagol);

		// A companion, a possession, an event and a Shadow card: any may be discarded, and one must be.
		assertTrue(scn.FreepsHasCardChoiceAvailable(aragorn, brand, sleep, troop));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(sleep);
		assertEquals(3, scn.GetFreepsHandCount());
		assertInZone(Zone.DISCARD, sleep);
		assertInZone(Zone.HAND, aragorn, brand, troop);
		// Sméagol alone: Sam, waiting for his own skirmish, and the minion are as they were.
		assertEquals(4, scn.GetStrength(smeagol));
		assertEquals(3, scn.GetStrength(sam));
		assertEquals(5, scn.GetStrength(savage));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void UsedInAnotherSkirmishItIsPlusOneThereAndGoneInHisOwn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var smeagol = scn.GetFreepsCard("smeagol");
		var sam = scn.GetFreepsCard("sam");
		var brand = scn.GetFreepsCard("brand");
		var sleep = scn.GetFreepsCard("sleep");
		scn.MoveCompanionsToTable(smeagol, sam);

		var savage = scn.GetShadowCard("savage");
		var runner = scn.GetShadowCard("runner");

		scn.StartGame();
		scn.MoveMinionsToTable(savage, runner);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{smeagol, savage}, new PhysicalCardImpl[]{sam, runner});
		scn.SetFreepsHand(brand, sleep);

		// Sam's skirmish first: Sméagol is assigned, but not skirmishing; the text names no skirmish of his.
		scn.FreepsResolveSkirmish(sam);
		assertEquals(3, scn.GetStrength(smeagol));
		assertTrue(scn.FreepsActionAvailable(smeagol));
		scn.FreepsUseCardAction(smeagol);
		scn.FreepsChooseCard(sleep);
		assertInZone(Zone.DISCARD, sleep);
		assertEquals(4, scn.GetStrength(smeagol));
		assertEquals(3, scn.GetStrength(sam));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		// 3 against 5: Sam loses and lives.
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(sam));

		// A skirmish action lasts for its own skirmish only.
		scn.FreepsResolveSkirmish(smeagol);
		assertEquals(3, scn.GetStrength(smeagol));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void WithAnEmptyHandTheSkirmishAbilityIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var smeagol = scn.GetFreepsCard("smeagol");
		var brand = scn.GetFreepsCard("brand");
		scn.MoveCompanionsToTable(smeagol);

		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(smeagol, savage);
		scn.SetFreepsHand();
		scn.FreepsResolveSkirmish(smeagol);

		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(0, scn.GetFreepsHandCount());
		assertFalse(scn.FreepsActionAvailable(smeagol));

		// One card put in hand and the decision sent again: offered, and the lone card discarded with nobody asked.
		scn.SetFreepsHand(brand);
		scn.FreepsRefreshDecision();
		assertInHand(brand);
		assertTrue(scn.FreepsActionAvailable(smeagol));
		scn.FreepsUseCardAction(smeagol);
		assertInZone(Zone.DISCARD, brand);
		assertEquals(0, scn.GetFreepsHandCount());
		assertEquals(4, scn.GetStrength(smeagol));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}
}
