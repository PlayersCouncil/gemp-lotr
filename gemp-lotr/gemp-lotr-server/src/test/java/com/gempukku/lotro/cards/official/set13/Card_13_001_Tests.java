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

public class Card_13_001_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("arod", "13_1");
					put("gimli", "1_13");       // Gimli, Son of Gloin: Dwarf companion, strength 6, vitality 3; a skirmish ability only
					put("farin", "1_11");       // Farin, Dwarven Emissary: Dwarf companion, strength 5 (+2 skirmishing an Orc), vitality 3
					put("grimir", "1_17");      // Grimir, Dwarven Elder: Dwarf ally; a fellowship ability only
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a companion who is no Dwarf; a maneuver ability only
					// Wish For Our Kinfolk: [dwarven] support-area condition that adds [dwarven] tokens to itself by
					// its fellowship ability (used only on purpose)
					put("wish1", "8_8");
					put("wish2", "8_8");
					// Run Until Found: [dwarven] support-area condition; a token only as it is played, so cheated in it has none
					put("rununtil", "18_2");
					// Garrison of Osgiliath: [gondor] support-area condition that takes 2 [gondor] tokens as it is played
					put("garrison", "6_52");
					put("axe", "1_9");          // Dwarven Axe: [dwarven] hand weapon a Dwarf bears; its trigger needs a skirmish won
					put("marksman", "1_176");   // Goblin Marksman: [moria] Orc archer, strength 7, vitality 1, no other text
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc, strength 13, no text
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void ArodStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 13
		 * Name: Arod, Rohirrim Steed
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Dwarven
		 * Twilight Cost: 1
		 * Type: Possession
		 * Subtype: Mount
		 * Resistance: +1
		 * Game Text: Bearer must be a Dwarf.<br>When you play Arod, you may reinforce a [dwarven] token.<br><b>Response:</b> If bearer is about to take a wound during a skirmish, remove 2 [dwarven] tokens to prevent that.
		*/

		var scn = GetScenario();

		var arod = scn.GetFreepsCard("arod");

		assertEquals("Arod", arod.getBlueprint().getTitle());
		assertEquals("Rohirrim Steed", arod.getBlueprint().getSubtitle());
		assertTrue(arod.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, arod.getBlueprint().getSide());
		assertEquals(Culture.DWARVEN, arod.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, arod.getBlueprint().getCardType());
		assertTrue(arod.getBlueprint().getPossessionClasses().contains(PossessionClass.MOUNT));
		assertEquals(1, arod.getBlueprint().getTwilightCost());
		assertEquals(1, arod.getBlueprint().getResistance());
	}

	@Test
	public void PlayedOnADwarfItMayReinforceADwarvenTokenOnACardThatHasOne() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var arod = scn.GetFreepsCard("arod");
		var gimli = scn.GetFreepsCard("gimli");
		var grimir = scn.GetFreepsCard("grimir");
		var aragorn = scn.GetFreepsCard("aragorn");
		var wish1 = scn.GetFreepsCard("wish1");
		var wish2 = scn.GetFreepsCard("wish2");
		var rununtil = scn.GetFreepsCard("rununtil");
		var garrison = scn.GetFreepsCard("garrison");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(gimli, aragorn);
		scn.MoveAlliesToTable(grimir);
		scn.MoveCardsToSupportArea(wish1, wish2, rununtil, garrison);
		scn.AddTokensToCard(wish1, 2);
		scn.AddTokensToCard(wish2, 1);
		scn.AddTokensToCard(garrison, 2);
		scn.MoveCardsToHand(arod);

		scn.StartGame();

		assertEquals(2, scn.GetCultureTokensOn(wish1));
		assertEquals(1, scn.GetCultureTokensOn(wish2));
		assertEquals(0, scn.GetCultureTokensOn(rununtil));
		assertEquals(2, scn.GetCultureTokensOn(garrison));
		scn.FreepsPlayCard(arod);
		// A Dwarf companion and a Dwarf ally may bear it; Aragorn and Frodo are no Dwarves.
		assertTrue(scn.FreepsHasCardChoiceAvailable(gimli, grimir));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(aragorn, frodo));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(grimir);
		assertAttachedTo(arod, grimir);

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(arod));
		scn.FreepsAcceptOptionalTrigger();
		// Either [dwarven] card that has a [dwarven] token; not the [dwarven] card with none, nor the card with
		// [gondor] tokens.
		assertTrue(scn.FreepsHasCardChoiceAvailable(wish1, wish2));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(rununtil, garrison));
		scn.FreepsChooseCard(wish2);
		assertEquals(2, scn.GetCultureTokensOn(wish1));
		assertEquals(2, scn.GetCultureTokensOn(wish2));
		assertEquals(0, scn.GetCultureTokensOn(rununtil));
		assertEquals(2, scn.GetCultureTokensOn(garrison));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void TheReinforcementMayBeDeclinedAndAnotherPossessionPlayedAfterArodIsNotAnswered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var arod = scn.GetFreepsCard("arod");
		var gimli = scn.GetFreepsCard("gimli");
		var axe = scn.GetFreepsCard("axe");
		var wish1 = scn.GetFreepsCard("wish1");
		scn.MoveCompanionsToTable(gimli);
		scn.MoveCardsToSupportArea(wish1);
		scn.AddTokensToCard(wish1, 1);
		scn.MoveCardsToHand(arod, axe);

		scn.StartGame();

		assertEquals(1, scn.GetCultureTokensOn(wish1));
		// Gimli is the only Dwarf, so Arod goes on him unasked.
		scn.FreepsPlayCard(arod);
		assertAttachedTo(arod, gimli);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(arod));
		scn.FreepsDeclineOptionalTrigger();
		assertEquals(1, scn.GetCultureTokensOn(wish1));
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// Another [dwarven] possession played after Arod, with the token still there to reinforce.
		scn.FreepsPlayCard(axe);
		assertAttachedTo(axe, gimli);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(arod));
		assertEquals(1, scn.GetCultureTokensOn(wish1));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithNoDwarvenTokenToReinforceTheReinforcementIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var arod = scn.GetFreepsCard("arod");
		var gimli = scn.GetFreepsCard("gimli");
		var rununtil = scn.GetFreepsCard("rununtil");
		var garrison = scn.GetFreepsCard("garrison");
		scn.MoveCompanionsToTable(gimli);
		// A [dwarven] card with no token, and [gondor] tokens on a [gondor] card.
		scn.MoveCardsToSupportArea(rununtil, garrison);
		scn.AddTokensToCard(garrison, 2);
		scn.MoveCardsToHand(arod);

		scn.StartGame();

		assertEquals(0, scn.GetCultureTokensOn(rununtil));
		assertEquals(2, scn.GetCultureTokensOn(garrison));
		scn.FreepsPlayCard(arod);
		assertAttachedTo(arod, gimli);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(arod));
		assertEquals(0, scn.GetCultureTokensOn(rununtil));
		assertEquals(2, scn.GetCultureTokensOn(garrison));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ResponseRemovesTwoDwarvenTokensToPreventASkirmishWoundOnBearerButNotAnArcheryWoundOrAnotherDwarfsWound() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var arod = scn.GetFreepsCard("arod");
		var gimli = scn.GetFreepsCard("gimli");
		var farin = scn.GetFreepsCard("farin");
		var wish1 = scn.GetFreepsCard("wish1");
		var wish2 = scn.GetFreepsCard("wish2");
		var garrison = scn.GetFreepsCard("garrison");
		var marksman = scn.GetShadowCard("marksman");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(gimli, farin);
		scn.AttachCardsTo(gimli, arod);
		scn.MoveCardsToSupportArea(wish1, wish2, garrison);
		scn.AddTokensToCard(wish1, 2);
		scn.AddTokensToCard(wish2, 1);
		scn.AddTokensToCard(garrison, 2);

		scn.StartGame();
		assertAttachedTo(arod, gimli);
		assertEquals(2, scn.GetCultureTokensOn(wish1));
		assertEquals(1, scn.GetCultureTokensOn(wish2));
		assertEquals(2, scn.GetCultureTokensOn(garrison));
		scn.MoveMinionsToTable(marksman, troop);
		scn.SkipToPhase(Phase.ARCHERY);
		scn.PassCurrentPhaseActions();

		// The Marksman's archery wound, placed on Gimli in the archery phase, is not during a skirmish.
		assertAttachedTo(arod, gimli);
		assertEquals(0, scn.GetWoundsOn(gimli));
		scn.FreepsChooseCard(gimli);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(arod));
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertEquals(2, scn.GetCultureTokensOn(wish1));
		assertEquals(1, scn.GetCultureTokensOn(wish2));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());

		scn.PassCurrentPhaseActions();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{farin, troop},
				new PhysicalCardImpl[]{gimli, marksman});

		// Farin (5 + 2 against an Orc) loses to the Troop (13): his wound is no wound on bearer.
		scn.FreepsResolveSkirmish(farin);
		scn.PassSkirmishActions();
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(arod));
		assertEquals(1, scn.GetWoundsOn(farin));
		assertEquals(2, scn.GetCultureTokensOn(wish1));
		assertEquals(1, scn.GetCultureTokensOn(wish2));

		// Gimli (6) loses to the Marksman (7): bearer is about to take a wound during a skirmish.
		scn.FreepsResolveSkirmish(gimli);
		scn.PassSkirmishActions();
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(arod));
		scn.FreepsAcceptOptionalTrigger();
		// The two tokens come one card at a time: the [dwarven] cards, never the [gondor] one.
		assertTrue(scn.FreepsHasCardChoiceAvailable(wish1, wish2));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(garrison));
		scn.FreepsChooseCard(wish2);
		assertEquals(1, scn.GetCultureTokensOn(wish1));
		assertEquals(0, scn.GetCultureTokensOn(wish2));
		assertEquals(2, scn.GetCultureTokensOn(garrison));
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void ResponseIsNotOfferedWithOnlyOneDwarvenToken() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var arod = scn.GetFreepsCard("arod");
		var gimli = scn.GetFreepsCard("gimli");
		var wish1 = scn.GetFreepsCard("wish1");
		var garrison = scn.GetFreepsCard("garrison");
		var marksman = scn.GetShadowCard("marksman");
		scn.MoveCompanionsToTable(gimli);
		scn.AttachCardsTo(gimli, arod);
		scn.MoveCardsToSupportArea(wish1, garrison);
		scn.AddTokensToCard(wish1, 1);
		scn.AddTokensToCard(garrison, 2);

		scn.StartGame();
		assertAttachedTo(arod, gimli);
		assertEquals(1, scn.GetCultureTokensOn(wish1));
		assertEquals(2, scn.GetCultureTokensOn(garrison));
		scn.MoveMinionsToTable(marksman);
		scn.SkipToPhase(Phase.ARCHERY);
		scn.PassCurrentPhaseActions();
		// The archery wound goes on Frodo, so that Gimli is unwounded going in.
		scn.FreepsChooseCard(scn.GetRingBearer());
		scn.PassCurrentPhaseActions();
		scn.FreepsAssignToMinions(gimli, marksman);
		scn.FreepsResolveSkirmish(gimli);

		assertAttachedTo(arod, gimli);
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(1, scn.GetCultureTokensOn(wish1));
		assertEquals(2, scn.GetCultureTokensOn(garrison));
		// Gimli (6) loses to the Marksman (7); one [dwarven] token, beside two [gondor] ones, cannot pay.
		scn.PassSkirmishActions();
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(arod));
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertEquals(1, scn.GetCultureTokensOn(wish1));
		assertEquals(2, scn.GetCultureTokensOn(garrison));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
