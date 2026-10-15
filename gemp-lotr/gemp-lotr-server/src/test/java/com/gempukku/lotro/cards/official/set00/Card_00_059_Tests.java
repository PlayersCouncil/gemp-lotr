package com.gempukku.lotro.cards.official.set00;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_00_059_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("erkenbrand", "0_59");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetSkirmishScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("erkenbrand", "0_59");
					put("aldor", "4_262");      // Aldor, Soldier of Edoras: [rohan] Man ally; his archery ability needs a villager
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: the companion in the skirmish, a Man who is not [rohan]
					put("runner", "1_178");     // Goblin Runner: strength 5, the minion he fights
					put("scimitar", "1_180");   // Goblin Scimitar: Shadow possession on a [moria] Orc, strength +2
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetNoShadowPossessionScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("erkenbrand", "0_59");
					put("sword", "1_299");      // Hobbit Sword: a Free Peoples possession, no text but "Bearer must be a Hobbit"
					put("ambition", "1_133");   // Saruman's Ambition: a Shadow card in play that is a condition
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetDiscardScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("erkenbrand", "0_59");
					put("theoden", "4_292");     // Theoden, Son of Thengel: a [rohan] Man companion; a regroup ability only
					put("merry", "103_85");      // Merry, Master Holbytla: a [rohan] Hobbit, a Man only beside 3 [rohan] companions or Eowyn;
					                             // his skirmish ability is used only on purpose, and nobody exerts here
					put("aragorn", "1_89");      // Aragorn, Ranger of the North: a Man who is not [rohan]; a maneuver ability only
					put("gimli", "1_13");        // Gimli, Son of Gloin: a companion played after Erkenbrand; a skirmish ability only
					put("sword", "1_299");       // Hobbit Sword: a Free Peoples possession, "Bearer must be a Hobbit" and nothing else

					put("wildmen", "4_4");       // Band of Wild Men: [dunland] Man, strength 11; a trigger on winning a skirmish only
					put("ironaxe", "4_26");      // Iron Axe: [dunland] possession, "Bearer must be a [dunland] Man" and nothing else
					put("poison", "8_28");       // Spider Poison: [gollum] possession in the Shadow support area; its texts act only on a bearer
					put("palantir", "3_67");     // The Palantir of Orthanc: an [isengard] ARTIFACT in the support area; a Shadow ability only
					put("ambition", "1_133");    // Saruman's Ambition: an [isengard] condition in the support area; a discount and a skirmish ability used only on purpose
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void ErkenbrandStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 0
		 * Name: Erkenbrand, Master of Westfold
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Rohan
		 * Twilight Cost: 3
		 * Type: Companion
		 * Subtype: Man
		 * Strength: 7
		 * Vitality: 3
		 * Resistance: 5
		 * Game Text: If you can spot a [rohan] Man, you may play Erkenbrand any time you could play a skirmish event.<br>When you play Erkenbrand, you may discard a Shadow possession.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("erkenbrand");

		assertEquals("Erkenbrand", card.getBlueprint().getTitle());
		assertEquals("Master of Westfold", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ROHAN, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(7, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(5, card.getBlueprint().getResistance());
	}

	@Test
	public void WithARohanManHeIsPlayedDuringASkirmishAsASkirmishEventCouldBe() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetSkirmishScenario();

		var erkenbrand = scn.GetFreepsCard("erkenbrand");
		var aldor = scn.GetFreepsCard("aldor");
		var aragorn = scn.GetFreepsCard("aragorn");
		var runner = scn.GetShadowCard("runner");
		var scimitar = scn.GetShadowCard("scimitar");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveAlliesToTable(aldor);
		scn.MoveCardsToHand(erkenbrand);

		scn.StartGame();
		scn.MoveMinionsToTable(runner);
		scn.AttachCardsTo(runner, scimitar);
		assertInZone(Zone.HAND, erkenbrand);
		assertAttachedTo(scimitar, runner);

		// In the fellowship phase he may be played as any companion may.  He is left in hand.
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertTrue(scn.FreepsPlayAvailable(erkenbrand));

		// No skirmish event could be played in the maneuver phase, so neither can he.
		scn.SkipToPhase(Phase.MANEUVER);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(erkenbrand));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, runner);
		scn.FreepsResolveSkirmish(aragorn);

		// In Aragorn's skirmish, where a skirmish event could be played, Aldor is the [rohan] Man spotted.
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertTrue(scn.FreepsPlayAvailable(erkenbrand));
		assertInZone(Zone.HAND, erkenbrand);

		scn.FreepsPlayCard(erkenbrand);
		// He joins the fellowship, not the skirmish.
		assertInZone(Zone.FREE_CHARACTERS, erkenbrand);
		assertFalse(scn.IsCharSkirmishing(erkenbrand));
		assertTrue(scn.IsCharSkirmishingAgainst(aragorn, runner));

		// "When you play Erkenbrand" answers this play as it would one in the fellowship phase.  Declined, the
		// Scimitar stays where it is and the skirmish goes on with the Shadow player's action.
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(erkenbrand));
		scn.FreepsDeclineOptionalTrigger();
		assertAttachedTo(scimitar, runner);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	// Ketura on Q-0034: an optional trigger with no cost whose whole effect would do nothing is not offered.
	// The offer made, with a Shadow possession in play, is read in the test above.
	@Test
	public void WithNoShadowPossessionInPlayHisDiscardIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetNoShadowPossessionScenario();

		var erkenbrand = scn.GetFreepsCard("erkenbrand");
		var frodo = scn.GetRingBearer();
		var sword = scn.GetFreepsCard("sword");
		var ambition = scn.GetShadowCard("ambition");
		scn.MoveCardsToHand(erkenbrand);
		// A possession that is not a Shadow card, and a Shadow card that is no possession.
		scn.AttachCardsTo(frodo, sword);
		scn.MoveCardsToSupportArea(ambition);

		scn.StartGame();

		assertAttachedTo(sword, frodo);
		assertInZone(Zone.SUPPORT, ambition);
		assertTrue(scn.FreepsPlayAvailable(erkenbrand));

		scn.FreepsPlayCard(erkenbrand);
		assertInZone(Zone.FREE_CHARACTERS, erkenbrand);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertAttachedTo(sword, frodo);
		assertInZone(Zone.SUPPORT, ambition);
	}

	@Test
	public void WithARohanManCompanionHeIsPlayedInASkirmishAndDiscardsAChosenShadowPossession() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetDiscardScenario();

		var erkenbrand = scn.GetFreepsCard("erkenbrand");
		var theoden = scn.GetFreepsCard("theoden");
		var frodo = scn.GetRingBearer();
		var sword = scn.GetFreepsCard("sword");
		var wildmen = scn.GetShadowCard("wildmen");
		var ironaxe = scn.GetShadowCard("ironaxe");
		var poison = scn.GetShadowCard("poison");
		var palantir = scn.GetShadowCard("palantir");
		var ambition = scn.GetShadowCard("ambition");
		scn.MoveCompanionsToTable(theoden);
		scn.AttachCardsTo(frodo, sword);
		scn.MoveCardsToHand(erkenbrand);
		scn.MoveCardsToSupportArea(poison, palantir, ambition);

		scn.StartGame();
		scn.MoveMinionsToTable(wildmen);
		scn.AttachCardsTo(wildmen, ironaxe);
		assertInZone(Zone.HAND, erkenbrand);
		assertAttachedTo(ironaxe, wildmen);
		assertAttachedTo(sword, frodo);
		assertInZone(Zone.SUPPORT, poison, palantir, ambition);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(theoden, wildmen);
		scn.FreepsResolveSkirmish(theoden);

		// Theoden is the [rohan] Man spotted.
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertTrue(scn.FreepsPlayAvailable(erkenbrand));
		assertAttachedTo(ironaxe, wildmen);
		assertAttachedTo(sword, frodo);
		assertInZone(Zone.SUPPORT, poison, palantir, ambition);

		scn.FreepsPlayCard(erkenbrand);
		assertInZone(Zone.FREE_CHARACTERS, erkenbrand);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(erkenbrand));
		scn.FreepsAcceptOptionalTrigger();

		// A Shadow possession borne by a minion and one in the support area; not a Shadow artifact, a Shadow
		// condition, or a Free Peoples possession.
		assertTrue(scn.FreepsHasCardChoiceAvailable(ironaxe, poison));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(palantir, ambition, sword));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(ironaxe);

		assertInZone(Zone.DISCARD, ironaxe);
		assertInZone(Zone.SUPPORT, poison, palantir, ambition);
		assertAttachedTo(sword, frodo);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void WithOnlyARohanHobbitAndAGondorManHeCannotBePlayedInASkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetDiscardScenario();

		var erkenbrand = scn.GetFreepsCard("erkenbrand");
		var merry = scn.GetFreepsCard("merry");
		var aragorn = scn.GetFreepsCard("aragorn");
		var wildmen = scn.GetShadowCard("wildmen");
		scn.MoveCompanionsToTable(merry, aragorn);
		scn.MoveCardsToHand(erkenbrand);
		assertInZone(Zone.HAND, erkenbrand);

		scn.StartGame();
		scn.MoveMinionsToTable(wildmen);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, wildmen);
		scn.FreepsResolveSkirmish(aragorn);

		// Merry is [rohan] and no Man (one [rohan] companion, no Eowyn); Aragorn is a Man and not [rohan].
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertInZone(Zone.HAND, erkenbrand);
		assertFalse(scn.FreepsPlayAvailable(erkenbrand));
	}

	@Test
	public void NoDiscardIsOfferedWhenAnotherCompanionIsPlayedAfterHim() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetDiscardScenario();

		var erkenbrand = scn.GetFreepsCard("erkenbrand");
		var gimli = scn.GetFreepsCard("gimli");
		var poison = scn.GetShadowCard("poison");
		scn.MoveCompanionsToTable(erkenbrand);
		scn.MoveCardsToHand(gimli);
		scn.MoveCardsToSupportArea(poison);

		scn.StartGame();

		assertInZone(Zone.SUPPORT, poison);
		assertTrue(scn.FreepsPlayAvailable(gimli));
		scn.FreepsPlayCard(gimli);
		assertInZone(Zone.FREE_CHARACTERS, gimli);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(erkenbrand));
		assertInZone(Zone.SUPPORT, poison);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
