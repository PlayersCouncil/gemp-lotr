package com.gempukku.lotro.cards.official.set12;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_12_018_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("hadafang", "12_18");
					put("arwen", "1_30");       // Arwen, Daughter of Elrond: [elven] Elf companion, strength 6, vitality 3; +3 only against a Nazgul
					// Arwen, Lady of Rivendell: the Arwen who is an ally; her texts are about Aragorn only
					put("arwenally", "102_6");
					put("elrond", "1_40");      // Elrond, Lord of Rivendell: the Elrond who is an ally, strength 8, vitality 4
					put("elrondcomp", "12_17"); // Elrond, Witness to History: the Elrond who is a companion; a skirmish ability only
					put("legolas", "1_50");     // Legolas, Greenleaf: an [elven] Elf companion who is neither Arwen nor Elrond
					put("gimli", "1_13");       // Gimli, Son of Gloin: a companion, strength 6, damage +1; a skirmish ability only
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: exerts for One Whom Men Would Follow
					// One Whom Men Would Follow: Maneuver event; exert Aragorn to make an ally strength +2 and take part in skirmishes
					put("follow", "1_109");
					// No Stranger to the Shadows: [gondor] condition borne by a ranger (Arwen); each site's Shadow number -1
					put("nostranger", "1_108");
					put("lord", "1_21");        // Lord of Moria: [dwarven] condition in the support area; each Dwarf damage +1
					put("sword", "1_299");      // Hobbit Sword: a Free Peoples possession on a Hobbit, strength +2, no other text
					// Weary: [isengard] condition on a companion; it answers only an assignment to an [isengard] tracker
					put("weary", "4_212");
					put("pinned", "1_192");     // Pinned Down: [moria] condition in the support area; archery total -1 with a [moria] archer

					put("pursuer", "4_190");    // Uruk Pursuer: fierce, strength 5, vitality 3; texts about unbound Hobbits only
					put("savage", "1_151");     // Uruk Savage: strength 5, vitality 3; Damage +1 is its whole text
					put("savage2", "1_151");
					put("raiders", "1_158");    // Uruk-hai Raiding Party: strength 9, vitality 3; Damage +1 is its whole text
					put("troop", "1_177");      // Goblin Patrol Troop: strength 13, vitality 3, no text
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void HadafangStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 12
		 * Name: Hadafang
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Elven
		 * Twilight Cost: 1
		 * Type: Possession
		 * Subtype: Hand weapon
		 * Strength: +2
		 * Game Text: Bearer must be Arwen or Elrond.<br>Each time bearer wins a skirmish, you may exert him or her to discard a condition from play.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("hadafang");

		assertEquals("Hadafang", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ELVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertTrue(card.getBlueprint().getPossessionClasses().contains(PossessionClass.HAND_WEAPON));
		assertEquals(1, card.getBlueprint().getTwilightCost());
		assertEquals(2, card.getBlueprint().getStrength());
	}

	@Test
	public void PlaysOnArwenOrTheAllyElrondAndNotOnAnotherElf() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var hadafang = scn.GetFreepsCard("hadafang");
		var arwen = scn.GetFreepsCard("arwen");
		var elrond = scn.GetFreepsCard("elrond");
		var legolas = scn.GetFreepsCard("legolas");
		scn.MoveCompanionsToTable(arwen, legolas);
		scn.MoveCardsToHand(hadafang);

		scn.StartGame();
		scn.MoveAlliesToTable(elrond);
		scn.FreepsRefreshDecision();

		assertTrue(scn.FreepsPlayAvailable(hadafang));
		scn.FreepsPlayCard(hadafang);

		// Arwen, a companion, and Elrond, an ally; not Legolas, an Elf companion of the same culture, nor Frodo.
		assertTrue(scn.FreepsHasCardChoiceAvailable(arwen, elrond));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(legolas, frodo));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(elrond);

		assertAttachedTo(hadafang, elrond);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void CannotBePlayedWithoutArwenOrElrondAndGoesOnTheCompanionElrondAlone() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hadafang = scn.GetFreepsCard("hadafang");
		var elrond = scn.GetFreepsCard("elrondcomp");
		var legolas = scn.GetFreepsCard("legolas");
		scn.MoveCompanionsToTable(legolas);
		scn.MoveCardsToHand(hadafang);

		scn.StartGame();

		// Legolas and Frodo: nobody may bear it.
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(hadafang));

		scn.MoveCompanionsToTable(elrond);
		scn.FreepsRefreshDecision();
		assertTrue(scn.FreepsPlayAvailable(hadafang));

		scn.FreepsPlayCard(hadafang);
		assertAttachedTo(hadafang, elrond);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void GoesOnTheAllyArwenAlone() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hadafang = scn.GetFreepsCard("hadafang");
		var arwen = scn.GetFreepsCard("arwenally");
		var legolas = scn.GetFreepsCard("legolas");
		scn.MoveCompanionsToTable(legolas);
		scn.MoveAlliesToTable(arwen);
		scn.MoveCardsToHand(hadafang);

		scn.StartGame();

		assertTrue(scn.FreepsPlayAvailable(hadafang));
		scn.FreepsPlayCard(hadafang);
		assertAttachedTo(hadafang, arwen);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void EachTimeArwenBearingItWinsASkirmishSheMayExertToDiscardAConditionFromPlay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var hadafang = scn.GetFreepsCard("hadafang");
		var arwen = scn.GetFreepsCard("arwen");
		var nostranger = scn.GetFreepsCard("nostranger");
		var lord = scn.GetFreepsCard("lord");
		var sword = scn.GetFreepsCard("sword");
		scn.MoveCompanionsToTable(arwen);
		scn.AttachCardsTo(arwen, nostranger);
		scn.AttachCardsTo(frodo, sword);
		scn.MoveCardsToSupportArea(lord);
		scn.MoveCardsToHand(hadafang);

		var weary = scn.GetShadowCard("weary");
		var pinned = scn.GetShadowCard("pinned");
		var pursuer = scn.GetShadowCard("pursuer");
		scn.AttachCardsTo(frodo, weary);
		scn.MoveCardsToSupportArea(pinned);

		scn.StartGame();
		// Arwen alone may bear it: it goes on her with nothing asked.
		scn.FreepsPlayCard(hadafang);
		assertAttachedTo(hadafang, arwen);
		assertAttachedTo(nostranger, arwen);
		assertAttachedTo(sword, frodo);
		assertAttachedTo(weary, frodo);
		assertInZone(Zone.SUPPORT, lord, pinned);

		scn.MoveMinionsToTable(pursuer);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(arwen, pursuer);
		scn.FreepsResolveSkirmish(arwen);
		scn.PassSkirmishActions();

		// Arwen 6 + 2 against the Pursuer's 5: she wins, and the Pursuer takes 1 wound.
		// Engine pass 54: what answers the win comes before the loser loses.
		assertEquals(0, scn.GetWoundsOn(pursuer));
		assertEquals(0, scn.GetWoundsOn(arwen));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(hadafang));

		scn.FreepsAcceptOptionalTrigger();
		assertEquals(1, scn.GetWoundsOn(arwen));
		// Conditions of either side, borne and in a support area; not a possession.
		assertTrue(scn.FreepsHasCardChoiceAvailable(nostranger, weary, lord, pinned));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(sword, hadafang));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(weary);

		assertInDiscard(weary);
		assertAttachedTo(nostranger, arwen);
		assertAttachedTo(sword, frodo);
		assertInZone(Zone.SUPPORT, lord, pinned);

		// The fierce round: Arwen wins again and is offered again.
		assertTrue(scn.InFierceRound());
		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(arwen, pursuer);
		scn.FreepsResolveSkirmish(arwen);
		scn.PassSkirmishActions();
		// Engine pass 54: what answers the win comes before the loser loses.
		assertEquals(1, scn.GetWoundsOn(pursuer));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(hadafang));

		scn.FreepsAcceptOptionalTrigger();
		assertEquals(2, scn.GetWoundsOn(arwen));
		assertTrue(scn.FreepsHasCardChoiceAvailable(nostranger, lord, pinned));
		scn.FreepsChooseCard(pinned);

		assertInDiscard(pinned);
		assertAttachedTo(nostranger, arwen);
		assertInZone(Zone.SUPPORT, lord);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void TheAllyElrondBearingItWhoWinsASkirmishExertsToDiscardACondition() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hadafang = scn.GetFreepsCard("hadafang");
		var elrond = scn.GetFreepsCard("elrond");
		var aragorn = scn.GetFreepsCard("aragorn");
		var follow = scn.GetFreepsCard("follow");
		var lord = scn.GetFreepsCard("lord");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToSupportArea(lord);
		scn.MoveCardsToHand(follow);

		var raiders = scn.GetShadowCard("raiders");

		scn.StartGame();
		scn.MoveAlliesToTable(elrond);
		scn.AttachCardsTo(elrond, hadafang);
		scn.MoveMinionsToTable(raiders);
		assertInZone(Zone.SUPPORT, lord);
		scn.SkipToPhase(Phase.MANEUVER);

		// Aragorn exerts, and Elrond, the one ally, is strength +2 and takes part in skirmishes.
		scn.FreepsPlayCard(follow);
		assertEquals(1, scn.GetWoundsOn(aragorn));
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(elrond, raiders);
		scn.FreepsResolveSkirmish(elrond);
		scn.PassSkirmishActions();

		// Elrond 8 + 2 + 2 against the Raiding Party's 9: he wins.
		// Engine pass 54: what answers the win comes before the loser loses.
		assertEquals(0, scn.GetWoundsOn(raiders));
		assertEquals(0, scn.GetWoundsOn(elrond));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(hadafang));

		// The one condition in play is discarded with nothing asked.
		scn.FreepsAcceptOptionalTrigger();
		assertEquals(1, scn.GetWoundsOn(elrond));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertInDiscard(lord);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void AnotherCompanionWinningOffersNothingAndDecliningLeavesEverythingAsItWas() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var hadafang = scn.GetFreepsCard("hadafang");
		var arwen = scn.GetFreepsCard("arwen");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(arwen, gimli);
		scn.AttachCardsTo(arwen, hadafang);

		var weary = scn.GetShadowCard("weary");
		var savage = scn.GetShadowCard("savage");
		var savage2 = scn.GetShadowCard("savage2");
		scn.AttachCardsTo(frodo, weary);

		scn.StartGame();
		assertAttachedTo(weary, frodo);
		scn.MoveMinionsToTable(savage, savage2);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{gimli, savage}, new PhysicalCardImpl[]{arwen, savage2});

		// Gimli's 6 against a Savage's 5: Gimli wins, and the Savage takes 1 wound and 1 for Gimli's damage +1.
		scn.FreepsResolveSkirmish(gimli);
		scn.PassSkirmishActions();
		assertEquals(2, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(hadafang));
		assertTrue(scn.FreepsDecisionAvailable("Choose next skirmish to resolve"));

		scn.FreepsResolveSkirmish(arwen);
		scn.PassSkirmishActions();
		// Engine pass 54: what answers the win comes before the loser loses.
		assertEquals(0, scn.GetWoundsOn(savage2));
		assertEquals(0, scn.GetWoundsOn(arwen));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(hadafang));

		scn.FreepsDeclineOptionalTrigger();
		assertEquals(0, scn.GetWoundsOn(arwen));
		assertAttachedTo(weary, frodo);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void ArwenLosingASkirmishOffersNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var hadafang = scn.GetFreepsCard("hadafang");
		var arwen = scn.GetFreepsCard("arwen");
		scn.MoveCompanionsToTable(arwen);
		scn.AttachCardsTo(arwen, hadafang);

		var weary = scn.GetShadowCard("weary");
		var troop = scn.GetShadowCard("troop");
		scn.AttachCardsTo(frodo, weary);

		scn.StartGame();
		assertAttachedTo(weary, frodo);
		scn.MoveMinionsToTable(troop);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(arwen, troop);
		assertEquals(0, scn.GetWoundsOn(arwen));
		scn.FreepsResolveSkirmish(arwen);
		scn.PassSkirmishActions();

		// The Troop's 13 beats Arwen's 6 + 2, but not by double: she takes 1 wound.
		assertEquals(1, scn.GetWoundsOn(arwen));
		assertEquals(0, scn.GetWoundsOn(troop));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(hadafang));
		assertAttachedTo(weary, frodo);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void AnExhaustedArwenIsNotOfferedTheTrigger() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var hadafang = scn.GetFreepsCard("hadafang");
		var arwen = scn.GetFreepsCard("arwen");
		scn.MoveCompanionsToTable(arwen);
		scn.AttachCardsTo(arwen, hadafang);

		var weary = scn.GetShadowCard("weary");
		var savage = scn.GetShadowCard("savage");
		scn.AttachCardsTo(frodo, weary);

		scn.StartGame();
		// Vitality 3 with 2 wounds: exhausted.
		scn.AddWoundsToChar(arwen, 2);
		assertEquals(2, scn.GetWoundsOn(arwen));
		assertAttachedTo(weary, frodo);
		scn.MoveMinionsToTable(savage);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(arwen, savage);
		scn.FreepsResolveSkirmish(arwen);
		scn.PassSkirmishActions();

		// She still wins, on strength.
		assertEquals(1, scn.GetWoundsOn(savage));
		assertEquals(2, scn.GetWoundsOn(arwen));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(hadafang));
		assertAttachedTo(weary, frodo);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
