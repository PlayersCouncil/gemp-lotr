package com.gempukku.lotro.cards.official.set02;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.StrengthModifier;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_02_017_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("dismay", "2_17");
					put("dismay2", "2_17");
					put("legolas", "1_50");     // Legolas, Greenleaf: Elf archer companion, strength 6, vitality 3; an archery ability used only on purpose
					put("arwen", "1_30");       // Arwen, Daughter of Elrond: an Elf companion who is no archer, strength 6
					// Garrison of Gondor: an archer companion who is no Elf ([gondor] Man), strength 8; cheated in past
					// "spot 3 [gondor] companions", and defender only beside a fortification
					put("garrison", "8_36");
					// Orophin, Lorien Bowman: an Elf archer ally, strength 3; an archery ability used only on purpose
					put("orophin", "1_56");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: exerted for One Whom Men Would Follow
					// One Whom Men Would Follow: Maneuver event; exert Aragorn to make an ally strength +2 and take part
					// in archery fire and skirmishes until the regroup phase
					put("follow", "1_109");

					put("savage1", "1_151");    // Uruk Savage: strength 5, vitality 3; Damage +1 is its whole text
					put("savage2", "1_151");
					put("runner", "1_178");     // Goblin Runner: strength 5, vitality 1; its text is about playing it; in no skirmish
					put("scrabbler", "2_64");   // Goblin Scrabbler: strength 4, vitality 1; its text is about playing it
					// Goblin Domain: a Shadow support condition, no minion; its text reaches companions played at site 4 or higher
					put("domain", "1_175");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void DismayOurEnemiesStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 2
		 * Name: Dismay Our Enemies
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Elven
		 * Twilight Cost: 0
		 * Type: Event
		 * Subtype: Response
		 * Game Text: <b>Response:</b> If an Elf archer wins a skirmish, make an opponent choose a minion to discard. That opponent may remove (3) to prevent this.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("dismay");

		assertEquals("Dismay Our Enemies", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ELVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.RESPONSE));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void WhenAnElfArcherWinsTheOpponentChoosesAMinionToDiscardIfHeDoesNotPrevent() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var dismay = scn.GetFreepsCard("dismay");
		var legolas = scn.GetFreepsCard("legolas");
		var savage = scn.GetShadowCard("savage1");
		var runner = scn.GetShadowCard("runner");
		var domain = scn.GetShadowCard("domain");
		scn.MoveCompanionsToTable(legolas);
		scn.MoveCardsToHand(dismay);

		scn.StartGame();
		assertInHand(dismay);
		scn.MoveMinionsToTable(savage);
		scn.MoveCardsToSupportArea(domain);
		// Legolas's archery wound goes on the one minion unasked; the Runner comes down after it.
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.MoveMinionsToTable(runner);
		assertEquals(1, scn.GetWoundsOn(savage));
		assertInZone(Zone.SUPPORT, domain);
		scn.PassAssignmentActions();
		scn.FreepsAssignToMinions(legolas, savage);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(legolas);
		scn.SetTwilight(4);

		// 6 against 5: the Savage takes its second wound and lives.
		scn.PassSkirmishActions();
		// Engine pass 54: what answers the win comes before the loser loses.
		assertEquals(1, scn.GetWoundsOn(savage));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(dismay));
		scn.FreepsChooseOptionalTrigger(dismay);

		assertTrue(scn.ShadowDecisionAvailable("remove (3)"));
		scn.ShadowChooseNo();

		// The opponent chooses: the minion just beaten or one in no skirmish; not his condition, not a companion.
		assertTrue(scn.ShadowHasCardChoiceAvailable(savage, runner));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(domain, legolas));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(runner);

		assertInDiscard(runner, dismay);
		assertInZone(Zone.SHADOW_CHARACTERS, savage);
		assertInZone(Zone.SUPPORT, domain);
		assertEquals(4, scn.GetTwilight());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void TheOpponentMayRemoveThreeTwilightToPreventTheDiscard() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var dismay = scn.GetFreepsCard("dismay");
		var legolas = scn.GetFreepsCard("legolas");
		var savage = scn.GetShadowCard("savage1");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(legolas);
		scn.MoveCardsToHand(dismay);

		scn.StartGame();
		assertInHand(dismay);
		scn.MoveMinionsToTable(savage);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.MoveMinionsToTable(runner);
		scn.PassAssignmentActions();
		scn.FreepsAssignToMinions(legolas, savage);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(legolas);
		scn.SetTwilight(4);
		scn.PassSkirmishActions();

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(dismay));
		scn.FreepsChooseOptionalTrigger(dismay);
		assertTrue(scn.ShadowDecisionAvailable("remove (3)"));
		scn.ShadowChooseYes();

		assertEquals(1, scn.GetTwilight());
		assertInZone(Zone.SHADOW_CHARACTERS, savage, runner);
		assertInDiscard(dismay);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WithFewerThanThreeTwilightTheOpponentIsNotAskedToPreventAndMustDiscard() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var dismay = scn.GetFreepsCard("dismay");
		var legolas = scn.GetFreepsCard("legolas");
		var savage = scn.GetShadowCard("savage1");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(legolas);
		scn.MoveCardsToHand(dismay);

		scn.StartGame();
		assertInHand(dismay);
		scn.MoveMinionsToTable(savage);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.MoveMinionsToTable(runner);
		scn.PassAssignmentActions();
		scn.FreepsAssignToMinions(legolas, savage);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(legolas);
		scn.SetTwilight(2);
		scn.PassSkirmishActions();

		scn.FreepsChooseOptionalTrigger(dismay);
		assertFalse(scn.ShadowDecisionAvailable("remove (3)"));
		assertTrue(scn.ShadowHasCardChoiceAvailable(savage, runner));
		scn.ShadowChooseCard(savage);

		assertInDiscard(savage, dismay);
		assertInZone(Zone.SHADOW_CHARACTERS, runner);
		assertEquals(2, scn.GetTwilight());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void TheOnlyMinionStillToDieOfTheLossIsTheOneToDiscardAndItsOwnerIsAskedToPrevent() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var dismay = scn.GetFreepsCard("dismay");
		var legolas = scn.GetFreepsCard("legolas");
		var savage = scn.GetShadowCard("savage1");
		scn.MoveCompanionsToTable(legolas);
		scn.MoveCardsToHand(dismay);

		scn.StartGame();
		assertInHand(dismay);
		scn.MoveMinionsToTable(savage);
		scn.AddWoundsToChar(savage, 1);
		assertEquals(1, scn.GetWoundsOn(savage));
		// The archery wound is its second; the wound for losing kills it.
		scn.SkipToAssignments();
		assertEquals(2, scn.GetWoundsOn(savage));
		scn.FreepsAssignToMinions(legolas, savage);
		scn.FreepsResolveSkirmish(legolas);
		scn.SetTwilight(4);
		scn.PassSkirmishActions();

		// The response to the win comes before the Savage's wound for losing (engine pass 54; until then the
		// Savage was dead of it, the event was played for nothing and no prevention was asked).
		assertInZone(Zone.SHADOW_CHARACTERS, savage);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(dismay));
		scn.FreepsChooseOptionalTrigger(dismay);

		// The one minion is chosen unasked, and its owner may remove (3) to prevent the discard.
		assertTrue(scn.ShadowDecisionAvailable("remove (3)"));
		scn.ShadowChooseNo();
		assertEquals(4, scn.GetTwilight());
		assertInDiscard(savage, dismay);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void ASecondCopyPlayedOnceTheOnlyMinionIsDiscardedIsPlayedForNothingAndNoPreventionIsAsked() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var dismay = scn.GetFreepsCard("dismay");
		var dismay2 = scn.GetFreepsCard("dismay2");
		var legolas = scn.GetFreepsCard("legolas");
		var savage = scn.GetShadowCard("savage1");
		scn.MoveCompanionsToTable(legolas);
		scn.MoveCardsToHand(dismay, dismay2);

		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(legolas, savage);
		scn.FreepsResolveSkirmish(legolas);
		scn.SetTwilight(4);
		scn.PassSkirmishActions();

		// The first copy discards the only minion, its owner declining to prevent it.
		scn.FreepsChooseOptionalTrigger(dismay);
		scn.ShadowChooseNo();
		assertInDiscard(savage, dismay);

		// The second copy answers the same win.  (Until engine pass 54 a loser killed by his wound for losing
		// left this board; now the win is answered first, and it is reached with a copy before.)  An event is
		// offered whatever its effect will find to do; with no minion to discard, no prevention is asked.
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(dismay2));
		scn.FreepsChooseOptionalTrigger(dismay2);
		assertFalse(scn.ShadowDecisionAvailable("remove (3)"));
		assertEquals(4, scn.GetTwilight());
		assertInDiscard(dismay2);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void IsNotOfferedWhenAnElfWhoIsNoArcherOrAnArcherWhoIsNoElfWins() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var dismay = scn.GetFreepsCard("dismay");
		var arwen = scn.GetFreepsCard("arwen");
		var garrison = scn.GetFreepsCard("garrison");
		var savage1 = scn.GetShadowCard("savage1");
		var savage2 = scn.GetShadowCard("savage2");
		scn.MoveCompanionsToTable(arwen, garrison);
		scn.MoveCardsToHand(dismay);

		scn.StartGame();
		assertInHand(dismay);
		scn.MoveMinionsToTable(savage1, savage2);
		// Garrison of Gondor's archery wound, placed by the Shadow player.
		scn.SkipToArcheryWounds();
		scn.ShadowChooseCard(savage1);
		scn.SkipToAssignments();
		assertEquals(1, scn.GetWoundsOn(savage1));
		assertEquals(0, scn.GetWoundsOn(savage2));
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{arwen, savage1}, new PhysicalCardImpl[]{garrison, savage2});

		// Arwen, an Elf and no archer, 6 against 5.
		scn.FreepsResolveSkirmish(arwen);
		scn.PassSkirmishActions();
		assertEquals(2, scn.GetWoundsOn(savage1));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(dismay));

		// Garrison of Gondor, an archer and no Elf, 8 against 5.
		scn.FreepsResolveSkirmish(garrison);
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(savage2));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(dismay));

		assertInHand(dismay);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void IsNotOfferedWhenTheElfArcherLosesASkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var dismay = scn.GetFreepsCard("dismay");
		var legolas = scn.GetFreepsCard("legolas");
		var savage = scn.GetShadowCard("savage1");
		scn.MoveCompanionsToTable(legolas);
		scn.MoveCardsToHand(dismay);

		scn.StartGame();
		assertInHand(dismay);
		scn.MoveMinionsToTable(savage);
		// 7 against Legolas's 6, so that he loses and lives (two wounds of his three vitality).
		scn.ApplyAdHocModifier(new StrengthModifier(null, savage, null, 2));
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(legolas, savage);
		scn.FreepsResolveSkirmish(legolas);
		assertEquals(0, scn.GetWoundsOn(legolas));
		scn.PassSkirmishActions();

		assertEquals(2, scn.GetWoundsOn(legolas));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(dismay));
		assertInHand(dismay);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void AnElfArcherAllyWhoWinsASkirmishMayBeAnsweredToo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var dismay = scn.GetFreepsCard("dismay");
		var orophin = scn.GetFreepsCard("orophin");
		var aragorn = scn.GetFreepsCard("aragorn");
		var follow = scn.GetFreepsCard("follow");
		var scrabbler = scn.GetShadowCard("scrabbler");
		var savage = scn.GetShadowCard("savage1");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveAlliesToTable(orophin);
		scn.MoveCardsToHand(dismay, follow);

		scn.StartGame();
		assertInHand(dismay);
		scn.MoveMinionsToTable(scrabbler, savage);
		scn.SkipToPhase(Phase.MANEUVER);
		// Orophin is made 5 and takes part in archery fire and skirmishes.
		scn.FreepsPlayCard(follow);
		assertEquals(5, scn.GetStrength(orophin));
		scn.PassManeuverActions();
		// His archery wound, placed by the Shadow player, goes on the Savage.
		scn.SkipToArcheryWounds();
		scn.ShadowChooseCard(savage);
		scn.SkipToAssignments();
		assertEquals(1, scn.GetWoundsOn(savage));
		scn.FreepsAssignToMinions(orophin, scrabbler);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(orophin);
		scn.SetTwilight(0);

		// 5 against 4.  The response to the win comes before the Scrabbler's wound for losing (engine pass 54;
		// until then the Scrabbler was dead of it, and the Savage was the one minion left to discard).
		scn.PassSkirmishActions();
		assertInZone(Zone.SHADOW_CHARACTERS, scrabbler);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(dismay));
		scn.FreepsChooseOptionalTrigger(dismay);

		// The opponent chooses, and may choose the Scrabbler, who would die of its wound anyway.
		assertTrue(scn.ShadowHasCardChoiceAvailable(scrabbler, savage));
		scn.ShadowChooseCard(scrabbler);
		assertInDiscard(scrabbler, dismay);
		assertInZone(Zone.SHADOW_CHARACTERS, savage);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
