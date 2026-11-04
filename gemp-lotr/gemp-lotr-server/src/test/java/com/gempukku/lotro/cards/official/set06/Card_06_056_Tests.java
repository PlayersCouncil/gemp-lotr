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

public class Card_06_056_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("trust", "6_56");
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
					// Rohirrim Herdsman: a [rohan] Man ally, vitality 2 ("to play, spot 2 [rohan] Men": cheated in); a
					// regroup ability used only on purpose
					put("herdsman", "7_247");
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
	public void TrustStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 6
		 * Name: Trust
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 0
		 * Type: Event
		 * Subtype: Skirmish
		 * Game Text: <b>Skirmish:</b> Make a [gondor] Man strength +2 (and heal a companion if you spot an [elven] token).
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("trust");

		assertEquals("Trust", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.SKIRMISH));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void MakesAGondorManStrongerForTheSkirmishAndWithAnElvenTokenHealsACompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var trust = scn.GetFreepsCard("trust");
		var knight1 = scn.GetFreepsCard("knight1");
		var knight2 = scn.GetFreepsCard("knight2");
		var merchant = scn.GetFreepsCard("merchant");
		var herdsman = scn.GetFreepsCard("herdsman");
		var pippin = scn.GetFreepsCard("pippin");
		var guard = scn.GetFreepsCard("guard");
		var agility = scn.GetFreepsCard("agility");
		var frodo = scn.GetRingBearer();
		var savage = scn.GetShadowCard("savage");
		var troop = scn.GetShadowCard("troop");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(knight1, knight2, pippin, guard);
		scn.MoveAlliesToTable(merchant, herdsman);
		scn.MoveCardsToSupportArea(agility);
		scn.MoveCardsToHand(trust);

		scn.StartGame();
		assertInZone(Zone.HAND, trust);
		scn.AddTokensToCard(agility, 1);
		scn.AddWoundsToChar(knight2, 2);
		scn.AddWoundsToChar(frodo, 1);
		scn.AddWoundsToChar(herdsman, 1);
		assertEquals(1, scn.GetCultureTokensOn(agility));
		assertEquals(2, scn.GetWoundsOn(knight2));
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetWoundsOn(herdsman));
		scn.MoveMinionsToTable(savage, troop, runner);
		scn.AddWoundsToChar(savage, 1);
		assertEquals(1, scn.GetWoundsOn(savage));
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{knight1, savage},
				new PhysicalCardImpl[]{knight2, troop},
				new PhysicalCardImpl[]{guard, runner}
		);
		scn.FreepsResolveSkirmish(knight1);

		assertEquals(6, scn.GetStrength(knight1));
		assertEquals(2, scn.GetWoundsOn(knight2));
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetWoundsOn(herdsman));
		assertEquals(1, scn.GetWoundsOn(savage));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertTrue(scn.FreepsPlayAvailable(trust));

		scn.FreepsPlayCard(trust);
		// Any [gondor] Man: the one skirmishing, one whose skirmish is still to come, and the ally; not a [gondor]
		// Hobbit or a [rohan] Man.
		assertTrue(scn.FreepsHasCardChoiceAvailable(knight1, knight2, merchant));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(pippin, guard, frodo, savage));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(knight1);
		assertEquals(8, scn.GetStrength(knight1));
		assertEquals(6, scn.GetStrength(knight2));
		assertEquals(6, scn.GetStrength(merchant));

		// An [elven] token can be spotted, so a companion is healed as well, with no "may": a wounded companion of
		// either race, not a wounded ally or minion.
		assertTrue(scn.FreepsHasCardChoiceAvailable(knight2, frodo));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(herdsman, savage));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(knight2);
		// One wound of two: a heal of one.
		assertEquals(1, scn.GetWoundsOn(knight2));
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetWoundsOn(herdsman));
		assertEquals(1, scn.GetWoundsOn(savage));
		assertEquals(1, scn.GetCultureTokensOn(agility));
		assertInDiscard(trust);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		scn.PassSkirmishActions();
		scn.FreepsResolveSkirmish(knight2);

		// The bonus named no duration and lasted the skirmish phase it was played in.
		assertEquals(6, scn.GetStrength(knight1));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void WithOnlyARohanTokenToSpotNoCompanionIsHealed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var trust = scn.GetFreepsCard("trust");
		var knight1 = scn.GetFreepsCard("knight1");
		var alive = scn.GetFreepsCard("alive");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCompanionsToTable(knight1);
		scn.MoveCardsToSupportArea(alive);
		scn.MoveCardsToHand(trust);

		scn.StartGame();
		assertInZone(Zone.HAND, trust);
		scn.AddTokensToCard(alive, 1);
		scn.AddWoundsToChar(knight1, 1);
		assertEquals(1, scn.GetCultureTokensOn(alive));
		assertEquals(1, scn.GetWoundsOn(knight1));
		scn.MoveMinionsToTable(savage);
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(knight1, savage);

		assertEquals(6, scn.GetStrength(knight1));
		assertEquals(1, scn.GetWoundsOn(knight1));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());

		scn.FreepsPlayCard(trust);
		// The Knight, the one [gondor] Man, is made stronger unasked; a token of another culture opens nothing.
		assertEquals(8, scn.GetStrength(knight1));
		assertEquals(1, scn.GetWoundsOn(knight1));
		assertEquals(1, scn.GetCultureTokensOn(alive));
		assertInDiscard(trust);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void WithAnElvenTokenAndNoWoundedCompanionTheGondorManIsStillMadeStronger() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var trust = scn.GetFreepsCard("trust");
		var knight1 = scn.GetFreepsCard("knight1");
		var herdsman = scn.GetFreepsCard("herdsman");
		var agility = scn.GetFreepsCard("agility");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCompanionsToTable(knight1);
		scn.MoveAlliesToTable(herdsman);
		scn.MoveCardsToSupportArea(agility);
		scn.MoveCardsToHand(trust);

		scn.StartGame();
		assertInZone(Zone.HAND, trust);
		scn.AddTokensToCard(agility, 1);
		scn.AddWoundsToChar(herdsman, 1);
		assertEquals(1, scn.GetCultureTokensOn(agility));
		assertEquals(1, scn.GetWoundsOn(herdsman));
		scn.MoveMinionsToTable(savage);
		scn.AddWoundsToChar(savage, 1);
		assertEquals(1, scn.GetWoundsOn(savage));
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(knight1, savage);

		assertEquals(6, scn.GetStrength(knight1));
		assertEquals(0, scn.GetWoundsOn(knight1));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());

		scn.FreepsPlayCard(trust);
		// No companion is wounded (the wounded ally and minion are no companions): that half does nothing, and the
		// rest is carried out.
		assertEquals(8, scn.GetStrength(knight1));
		assertEquals(1, scn.GetWoundsOn(herdsman));
		assertEquals(1, scn.GetWoundsOn(savage));
		assertInDiscard(trust);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void WithNoGondorManInPlayItIsPlayedForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var trust = scn.GetFreepsCard("trust");
		var guard = scn.GetFreepsCard("guard");
		var pippin = scn.GetFreepsCard("pippin");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCompanionsToTable(guard, pippin);
		scn.MoveCardsToHand(trust);

		scn.StartGame();
		assertInZone(Zone.HAND, trust);
		scn.MoveMinionsToTable(savage);
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(guard, savage);

		assertEquals(6, scn.GetStrength(guard));
		assertEquals(3, scn.GetStrength(pippin));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		// An event whose target finds nothing is still playable (F-0665).
		assertTrue(scn.FreepsPlayAvailable(trust));

		scn.FreepsPlayCard(trust);
		assertEquals(6, scn.GetStrength(guard));
		assertEquals(3, scn.GetStrength(pippin));
		assertInDiscard(trust);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void IsNotPlayableInTheAssignmentPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var trust = scn.GetFreepsCard("trust");
		var knight1 = scn.GetFreepsCard("knight1");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCompanionsToTable(knight1);
		scn.MoveCardsToHand(trust);

		scn.StartGame();
		assertInZone(Zone.HAND, trust);
		scn.MoveMinionsToTable(savage);
		scn.SkipToPhase(Phase.ASSIGNMENT);

		assertInZone(Zone.HAND, trust);
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(trust));
	}
}
