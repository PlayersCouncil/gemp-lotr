package com.gempukku.lotro.cards.official.set06;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_06_053_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("mortal", "6_53");
					// Gondorian Knight: [gondor] Man companion, strength 6, vitality 3; Knight is its whole text
					put("knight1", "5_35");
					put("knight2", "5_35");
					// Gondorian Merchant: the one [gondor] ally, a Man, strength 6, home 3T; a maneuver ability used only
					// on purpose
					put("merchant", "7_97");
					// Pippin, Trollslayer: a [gondor] Hobbit companion, no Man, strength 3 ("to play, spot a [gondor]
					// companion": cheated in); a skirmish ability used only on purpose
					put("pippin", "103_31");
					// Household Guard: a [rohan] Man companion, strength 6; Valiant is its whole text
					put("guard", "5_83");
					// We Left None Alive: a [rohan] support-area condition that holds [rohan] tokens (placed as it is
					// played, so cheated on); its skirmish ability is used only on purpose
					put("alive", "6_97");
					// Agility: an [elven] support-area condition that holds [elven] tokens (placed as it is played, so
					// cheated on); its skirmish ability is used only on purpose
					put("agility", "6_12");
					// Uruk Savage: [isengard] Uruk-hai, strength 5, vitality 3; Damage +1 is its whole text
					put("savage", "1_151");
					// Goblin Patrol Troop: [moria] Orc, strength 13, vitality 3, no text
					put("troop", "1_177");
					// Goblin Runner: [moria] Orc, strength 5, vitality 1, so always exhausted; its play trigger is not
					// reached by a cheat
					put("runner", "1_178");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void MortalMenStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 6
		 * Name: Mortal Men
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 0
		 * Type: Event
		 * Subtype: Skirmish
		 * Game Text: <b>Skirmish:</b> Make a [gondor] Man strength +2 (and exert a minion if you spot a [rohan] token).
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("mortal");

		assertEquals("Mortal Men", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.SKIRMISH));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void MakesAGondorManStrongerForTheSkirmishAndWithARohanTokenExertsAMinion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var mortal = scn.GetFreepsCard("mortal");
		var knight1 = scn.GetFreepsCard("knight1");
		var knight2 = scn.GetFreepsCard("knight2");
		var merchant = scn.GetFreepsCard("merchant");
		var pippin = scn.GetFreepsCard("pippin");
		var guard = scn.GetFreepsCard("guard");
		var alive = scn.GetFreepsCard("alive");
		var frodo = scn.GetRingBearer();
		var savage = scn.GetShadowCard("savage");
		var troop = scn.GetShadowCard("troop");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(knight1, knight2, pippin, guard);
		scn.MoveAlliesToTable(merchant);
		scn.MoveCardsToSupportArea(alive);
		scn.MoveCardsToHand(mortal);

		scn.StartGame();
		assertInZone(Zone.HAND, mortal);
		scn.AddTokensToCard(alive, 1);
		assertEquals(1, scn.GetCultureTokensOn(alive));
		scn.MoveMinionsToTable(savage, troop, runner);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{knight1, savage},
				new PhysicalCardImpl[]{knight2, troop},
				new PhysicalCardImpl[]{guard, runner}
		);
		scn.FreepsResolveSkirmish(knight1);

		assertEquals(6, scn.GetStrength(knight1));
		assertEquals(0, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetWoundsOn(troop));
		assertEquals(0, scn.GetWoundsOn(runner));
		assertInZone(Zone.HAND, mortal);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertTrue(scn.FreepsPlayAvailable(mortal));

		scn.FreepsPlayCard(mortal);
		// Any [gondor] Man: the one skirmishing, one whose skirmish is still to come, and the ally; not a [gondor]
		// Hobbit or a [rohan] Man.
		assertTrue(scn.FreepsHasCardChoiceAvailable(knight1, knight2, merchant));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(pippin, guard, frodo, savage));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(knight1);
		assertEquals(8, scn.GetStrength(knight1));
		assertEquals(6, scn.GetStrength(knight2));
		assertEquals(6, scn.GetStrength(merchant));

		// A [rohan] token can be spotted, so a minion is exerted as well, with no "may": one in this skirmish or
		// another, of any culture and race, and not the exhausted Runner or a companion.
		assertTrue(scn.FreepsHasCardChoiceAvailable(savage, troop));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(runner, knight1, guard));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(troop);
		assertEquals(1, scn.GetWoundsOn(troop));
		assertEquals(0, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetWoundsOn(runner));
		assertEquals(1, scn.GetCultureTokensOn(alive));
		assertInDiscard(mortal);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		scn.PassSkirmishActions();
		scn.FreepsResolveSkirmish(knight2);

		// The bonus named no duration and lasted the skirmish phase it was played in.
		assertEquals(6, scn.GetStrength(knight1));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void WithOnlyAnElvenTokenToSpotNoMinionIsExerted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var mortal = scn.GetFreepsCard("mortal");
		var knight1 = scn.GetFreepsCard("knight1");
		var agility = scn.GetFreepsCard("agility");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCompanionsToTable(knight1);
		scn.MoveCardsToSupportArea(agility);
		scn.MoveCardsToHand(mortal);

		scn.StartGame();
		assertInZone(Zone.HAND, mortal);
		scn.AddTokensToCard(agility, 1);
		assertEquals(1, scn.GetCultureTokensOn(agility));
		scn.MoveMinionsToTable(savage);
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(knight1, savage);

		assertEquals(6, scn.GetStrength(knight1));
		assertEquals(0, scn.GetWoundsOn(savage));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());

		scn.FreepsPlayCard(mortal);
		// The Knight, the one [gondor] Man, is made stronger unasked; a token of another culture opens nothing.
		assertEquals(8, scn.GetStrength(knight1));
		assertEquals(0, scn.GetWoundsOn(savage));
		assertEquals(1, scn.GetCultureTokensOn(agility));
		assertInDiscard(mortal);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void WithARohanTokenAndOnlyAnExhaustedMinionTheGondorManIsStillMadeStronger() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var mortal = scn.GetFreepsCard("mortal");
		var knight1 = scn.GetFreepsCard("knight1");
		var alive = scn.GetFreepsCard("alive");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(knight1);
		scn.MoveCardsToSupportArea(alive);
		scn.MoveCardsToHand(mortal);

		scn.StartGame();
		assertInZone(Zone.HAND, mortal);
		scn.AddTokensToCard(alive, 1);
		assertEquals(1, scn.GetCultureTokensOn(alive));
		scn.MoveMinionsToTable(runner);
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(knight1, runner);

		assertEquals(6, scn.GetStrength(knight1));
		assertEquals(0, scn.GetWoundsOn(runner));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());

		scn.FreepsPlayCard(mortal);
		// No minion can exert: that half does nothing, and the rest is carried out.
		assertEquals(8, scn.GetStrength(knight1));
		assertEquals(0, scn.GetWoundsOn(runner));
		assertInZone(Zone.SHADOW_CHARACTERS, runner);
		assertInDiscard(mortal);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void WithNoGondorManInPlayItIsPlayedForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var mortal = scn.GetFreepsCard("mortal");
		var guard = scn.GetFreepsCard("guard");
		var pippin = scn.GetFreepsCard("pippin");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCompanionsToTable(guard, pippin);
		scn.MoveCardsToHand(mortal);

		scn.StartGame();
		assertInZone(Zone.HAND, mortal);
		scn.MoveMinionsToTable(savage);
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(guard, savage);

		assertEquals(6, scn.GetStrength(guard));
		assertEquals(3, scn.GetStrength(pippin));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		// An event whose target finds nothing is still playable (F-0665).
		assertTrue(scn.FreepsPlayAvailable(mortal));

		scn.FreepsPlayCard(mortal);
		assertEquals(6, scn.GetStrength(guard));
		assertEquals(3, scn.GetStrength(pippin));
		assertInDiscard(mortal);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void IsNotPlayableInTheAssignmentPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var mortal = scn.GetFreepsCard("mortal");
		var knight1 = scn.GetFreepsCard("knight1");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCompanionsToTable(knight1);
		scn.MoveCardsToHand(mortal);

		scn.StartGame();
		assertInZone(Zone.HAND, mortal);
		scn.MoveMinionsToTable(savage);
		scn.SkipToPhase(Phase.ASSIGNMENT);

		assertInZone(Zone.HAND, mortal);
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(mortal));
	}
}
