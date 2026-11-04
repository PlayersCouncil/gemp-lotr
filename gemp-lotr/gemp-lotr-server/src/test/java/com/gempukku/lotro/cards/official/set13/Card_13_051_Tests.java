package com.gempukku.lotro.cards.official.set13;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_13_051_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("birthday", "13_51");
					put("gollum", "5_24");      // Gollum, Nasty Treacherous Creature: [gollum], strength 5; a regroup ability only
					put("wraithgollum", "103_66"); // Gollum, Half a Wraith Himself: the [wraith] Gollum, strength 5; abilities on purpose
					put("runner", "1_178");     // Goblin Runner: another minion, strength 5
					put("smeagol", "6_45");     // Sméagol, Poor Creature: the [gollum] Sméagol; a skirmish ability on purpose
					put("shiresmeagol", "103_116"); // Smeagol, Once Like Us: the [shire] Sméagol; an assignment ability on purpose
					put("deagol", "13_46");     // Déagol, Fateful Finder: the only card of that title, a [gollum] follower
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void ItsMyBirthdayStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 13
		 * Name: It's My Birthday
		 * Unique: False
		 * Side: Shadow
		 * Culture: Gollum
		 * Twilight Cost: 0
		 * Type: Event
		 * Subtype: Skirmish
		 * Game Text: Make Gollum strength +2 (or +4 if you can spot Sméagol or Déagol).
		*/

		var scn = GetScenario();

		var birthday = scn.GetFreepsCard("birthday");

		assertEquals("It's My Birthday", birthday.getBlueprint().getTitle());
		assertNull(birthday.getBlueprint().getSubtitle());
		assertFalse(birthday.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, birthday.getBlueprint().getSide());
		assertEquals(Culture.GOLLUM, birthday.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, birthday.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(birthday, Timeword.SKIRMISH));
		assertEquals(0, birthday.getBlueprint().getTwilightCost());
	}

	@Test
	public void MakesGollumStrengthPlus2WithNoSmeagolOrDeagolInPlay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var birthday = scn.GetShadowCard("birthday");
		var gollum = scn.GetShadowCard("gollum");
		var runner = scn.GetShadowCard("runner");
		var aragorn = scn.GetFreepsCard("aragorn");
		var deagol = scn.GetFreepsCard("deagol");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(birthday);
		// Déagol in a discard pile is no Déagol spotted.
		scn.MoveCardsToDiscard(deagol);

		scn.StartGame();
		assertInDiscard(deagol);
		assertInHand(birthday);
		scn.MoveMinionsToTable(gollum, runner);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, gollum);
		scn.ShadowAssignToMinions(aragorn, runner);
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertEquals(5, scn.GetStrength(gollum));
		assertEquals(5, scn.GetStrength(runner));
		assertInHand(birthday);
		assertTrue(scn.ShadowPlayAvailable(birthday));
		scn.ShadowPlayCard(birthday);

		// Gollum is the one card of his title in play: nothing is asked.
		assertEquals(7, scn.GetStrength(gollum));
		assertEquals(5, scn.GetStrength(runner));
		assertInDiscard(birthday);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void WithSmeagolSpottedGollumIsStrengthPlus4UntilTheEndOfThatSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var birthday = scn.GetShadowCard("birthday");
		var gollum = scn.GetShadowCard("gollum");
		var runner = scn.GetShadowCard("runner");
		var aragorn = scn.GetFreepsCard("aragorn");
		var smeagol = scn.GetFreepsCard("smeagol");
		scn.MoveCompanionsToTable(aragorn, smeagol);
		scn.MoveCardsToHand(birthday);

		scn.StartGame();
		scn.MoveMinionsToTable(gollum, runner);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, gollum}, new PhysicalCardImpl[]{smeagol, runner});
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertEquals(5, scn.GetStrength(gollum));
		scn.ShadowPlayCard(birthday);
		assertEquals(9, scn.GetStrength(gollum));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());

		// 9 against 8: Gollum wins.
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertTrue(scn.FreepsAwaitingSkirmishChoice());

		// The next skirmish is a phase of its own: the bonus is gone.
		scn.FreepsResolveSkirmish(smeagol);
		assertEquals(5, scn.GetStrength(gollum));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void TheWraithGollumWaitingForHisOwnSkirmishIsStrengthPlus4WithTheShireSmeagolSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var birthday = scn.GetShadowCard("birthday");
		var wraithgollum = scn.GetShadowCard("wraithgollum");
		var runner = scn.GetShadowCard("runner");
		var aragorn = scn.GetFreepsCard("aragorn");
		var shiresmeagol = scn.GetFreepsCard("shiresmeagol");
		scn.MoveCompanionsToTable(aragorn, shiresmeagol);
		scn.MoveCardsToHand(birthday);

		scn.StartGame();
		scn.MoveMinionsToTable(wraithgollum, runner);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, runner}, new PhysicalCardImpl[]{shiresmeagol, wraithgollum});
		// Aragorn's skirmish first: Gollum is in none yet.
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertEquals(5, scn.GetStrength(wraithgollum));
		assertEquals(5, scn.GetStrength(runner));
		scn.ShadowPlayCard(birthday);
		assertEquals(9, scn.GetStrength(wraithgollum));
		assertEquals(5, scn.GetStrength(runner));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void WithDeagolSpottedGollumIsStrengthPlus4() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var birthday = scn.GetShadowCard("birthday");
		var gollum = scn.GetShadowCard("gollum");
		var aragorn = scn.GetFreepsCard("aragorn");
		var deagol = scn.GetFreepsCard("deagol");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(aragorn);
		// Déagol borne by Frodo, with no Sméagol in play.
		scn.AttachCardsTo(frodo, deagol);
		scn.MoveCardsToHand(birthday);

		scn.StartGame();
		assertAttachedTo(deagol, frodo);
		scn.MoveMinionsToTable(gollum);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, gollum);
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertEquals(5, scn.GetStrength(gollum));
		scn.ShadowPlayCard(birthday);
		assertEquals(9, scn.GetStrength(gollum));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void WithNoGollumInPlayItIsPlayedForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var birthday = scn.GetShadowCard("birthday");
		var runner = scn.GetShadowCard("runner");
		var aragorn = scn.GetFreepsCard("aragorn");
		var smeagol = scn.GetFreepsCard("smeagol");
		scn.MoveCompanionsToTable(aragorn, smeagol);
		scn.MoveCardsToHand(birthday);

		scn.StartGame();
		scn.MoveMinionsToTable(runner);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, runner);
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		// CR 4.2 "effect": a card may be played even if it will have no effect.
		assertTrue(scn.ShadowPlayAvailable(birthday));
		scn.ShadowPlayCard(birthday);
		assertInDiscard(birthday);
		assertEquals(5, scn.GetStrength(runner));
		assertEquals(8, scn.GetStrength(aragorn));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void CannotBePlayedInTheAssignmentPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var birthday = scn.GetShadowCard("birthday");
		var gollum = scn.GetShadowCard("gollum");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(birthday);

		scn.StartGame();
		assertInHand(birthday);
		scn.MoveMinionsToTable(gollum);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
		assertInHand(birthday);
		assertFalse(scn.ShadowPlayAvailable(birthday));
	}
}
