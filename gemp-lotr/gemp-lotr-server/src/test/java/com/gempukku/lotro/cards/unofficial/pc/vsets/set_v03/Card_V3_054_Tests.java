package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v03;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.AddKeywordModifier;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_V3_054_Tests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("horn", "103_54");
					put("explorer", "4_250");     // Southron Explorer: Southron, strength 5, vitality 3; a regroup ability never used here
					put("explorer2", "4_250");
					put("lancers", "4_217");      // Desert Lancers: Southron, strength 11; a regroup ability never used here
					put("infantry", "4_227");     // Easterling Infantry: [raider] Man, Easterling and no Southron, strength 8; no text below 3 burdens
					put("troop", "1_177");        // Goblin Patrol Troop: [moria] Orc, strength 13, no text at all
					put("mumakil", "8_64");       // Mumakil: [raider] mount for a Southron, strength +3, fierce; damage +1 with initiative
					put("mumakil2", "8_64");
					put("warmumak", "5_78");      // War Mumak: [raider] mount for a Southron, cost 3, strength +4, fierce; an optional trigger on an overwhelm only
					put("mumak", "5_73");         // Mumak: [raider] mount for a Southron, cost 2, strength +3, fierce; an optional trigger on a win only
					put("charger", "103_41");     // Bladetusk Charger: a possession its Maneuver ability makes a fierce mounted Southron minion until the end of the turn

					put("aragorn", "1_89");       // Aragorn, Ranger of the North: strength 8, vitality 4
					put("horse", "4_283");        // Horse of Rohan: [rohan] mount; exerts minions at the start of its bearer's skirmishes
					put("sword", "1_112");        // Ranger's Sword: a [gondor] possession that is no mount
					put("orophin", "1_56");       // Orophin, Lorien Bowman: Elf ally (an archer, so only in a test that ends in the Shadow phase)
					put("asfaloth", "1_31");      // Asfaloth: [elven] mount, bearer must be an Elf
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void HornofFarHaradStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V3
		 * Name: Horn of Far Harad
		 * Unique: false
		 * Side: Shadow
		 * Culture: Raider
		 * Twilight Cost: 1
		 * Type: Possession
		 * Subtype: Horn
		 * Game Text: Bearer must be a mounted Southron.
		 * 	When you play this, hinder all Free Peoples mounts.
		 * 	Skirmish: Remove a threat to make a Southron strength +2.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("horn");

		assertEquals("Horn of Far Harad", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.RAIDER, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertTrue(card.getBlueprint().getPossessionClasses().contains(PossessionClass.HORN));
		assertEquals(1, card.getBlueprint().getPossessionClasses().size());
		assertEquals(1, card.getBlueprint().getTwilightCost());
		assertEquals(0, card.getBlueprint().getStrength());
		assertEquals(0, card.getBlueprint().getVitality());
	}

	@Test
	public void CannotBePlayedUntilASouthronIsMounted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var horn = scn.GetShadowCard("horn");
		var explorer = scn.GetShadowCard("explorer");
		var mumak = scn.GetShadowCard("mumak");
		scn.MoveCardsToHand(horn, mumak);

		scn.StartGame();
		scn.MoveMinionsToTable(explorer);
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertEquals(Phase.SHADOW, scn.GetCurrentPhase());
		// A Southron with no mount is the only minion in play.
		assertFalse(scn.ShadowPlayAvailable(horn));
		assertTrue(scn.ShadowPlayAvailable(mumak));

		// The Explorer is the only Southron, so the Mumak goes on him without a choice.
		scn.ShadowPlayCard(mumak);

		assertAttachedTo(mumak, explorer);
		assertTrue(scn.ShadowPlayAvailable(horn));

		scn.ShadowPlayCard(horn);

		assertAttachedTo(horn, explorer);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void PlayedForOneOnAMountedSouthronOfTheShadowPlayersChoiceAndHindersEveryFreePeoplesMount() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var horn = scn.GetShadowCard("horn");
		var explorer = scn.GetShadowCard("explorer");
		var explorer2 = scn.GetShadowCard("explorer2");
		var lancers = scn.GetShadowCard("lancers");
		var infantry = scn.GetShadowCard("infantry");
		var troop = scn.GetShadowCard("troop");
		var mumakil = scn.GetShadowCard("mumakil");
		var mumakil2 = scn.GetShadowCard("mumakil2");
		var warmumak = scn.GetShadowCard("warmumak");
		var mumak = scn.GetShadowCard("mumak");
		var aragorn = scn.GetFreepsCard("aragorn");
		var horse = scn.GetFreepsCard("horse");
		var sword = scn.GetFreepsCard("sword");
		var orophin = scn.GetFreepsCard("orophin");
		var asfaloth = scn.GetFreepsCard("asfaloth");
		scn.MoveCompanionsToTable(aragorn);
		scn.AttachCardsTo(aragorn, horse, sword);
		scn.MoveAlliesToTable(orophin);
		scn.AttachCardsTo(orophin, asfaloth);
		scn.MoveCardsToHand(horn);

		scn.StartGame();
		scn.MoveMinionsToTable(explorer, explorer2, lancers, infantry, troop);
		scn.AttachCardsTo(explorer, mumakil);
		scn.AttachCardsTo(lancers, warmumak);
		// No Easterling can be given a mount by legal play. This one is cheated on, to have a mounted
		// [raider] Man who is not a Southron.
		scn.AttachCardsTo(infantry, mumak);
		// Every Southron there is is a [raider] Man. An Orc is made a Southron and given a mount, to show that
		// the text goes by the keyword.
		scn.ApplyAdHocModifier(new AddKeywordModifier(null, troop, null, Keyword.SOUTHRON));
		scn.AttachCardsTo(troop, mumakil2);
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertEquals(Phase.SHADOW, scn.GetCurrentPhase());
		// 10 set + 2 for Frodo and Aragorn + 2 for the shadow number of site 2
		assertEquals(14, scn.GetTwilight());
		assertFalse(scn.IsHindered(horse));
		assertFalse(scn.IsHindered(asfaloth));
		assertFalse(scn.IsHindered(sword));
		assertTrue(scn.ShadowPlayAvailable(horn));

		scn.ShadowPlayCard(horn);

		// Three mounted Southrons to choose from; the Southron with no mount and the mounted Easterling are not.
		assertTrue(scn.ShadowHasCardChoiceAvailable(explorer, lancers, troop));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(explorer2, infantry));
		assertEquals(1, scn.ShadowGetChoiceMin());

		scn.ShadowChooseCard(lancers);

		assertAttachedTo(horn, lancers);
		// 14 - 1 for the Horn
		assertEquals(13, scn.GetTwilight());
		// Both Free Peoples mounts, on a companion and on an ally, of two cultures
		assertTrue(scn.IsHindered(horse));
		assertTrue(scn.IsHindered(asfaloth));
		// Not the possession that is no mount, not the Shadow player's own mounts, not the riders
		assertFalse(scn.IsHindered(sword));
		assertFalse(scn.IsHindered(mumakil));
		assertFalse(scn.IsHindered(mumakil2));
		assertFalse(scn.IsHindered(warmumak));
		assertFalse(scn.IsHindered(mumak));
		assertFalse(scn.IsHindered(aragorn));
		assertFalse(scn.IsHindered(orophin));
		assertFalse(scn.IsHindered(horn));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void PlayingAnotherPossessionWhileAHornIsInPlayHindersNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var horn = scn.GetShadowCard("horn");
		var explorer = scn.GetShadowCard("explorer");
		var lancers = scn.GetShadowCard("lancers");
		var mumakil = scn.GetShadowCard("mumakil");
		var warmumak = scn.GetShadowCard("warmumak");
		var aragorn = scn.GetFreepsCard("aragorn");
		var horse = scn.GetFreepsCard("horse");
		scn.MoveCompanionsToTable(aragorn);
		scn.AttachCardsTo(aragorn, horse);
		scn.MoveCardsToHand(warmumak);

		scn.StartGame();
		scn.MoveMinionsToTable(explorer, lancers);
		scn.AttachCardsTo(explorer, mumakil, horn);
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertEquals(Phase.SHADOW, scn.GetCurrentPhase());
		assertFalse(scn.IsHindered(horse));
		assertTrue(scn.ShadowPlayAvailable(warmumak));

		// The Explorer already has a mount, so the War Mumak goes on the Lancers without a choice.
		scn.ShadowPlayCard(warmumak);

		assertAttachedTo(warmumak, lancers);
		assertFalse(scn.IsHindered(horse));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void SkirmishAbilityRemovesAThreatEachTimeItIsUsedAndTheStrengthLastsUntilThatSkirmishEnds() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var horn = scn.GetShadowCard("horn");
		var explorer = scn.GetShadowCard("explorer");
		var explorer2 = scn.GetShadowCard("explorer2");
		var mumakil = scn.GetShadowCard("mumakil");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(explorer, explorer2);
		scn.AttachCardsTo(explorer, mumakil, horn);
		scn.AddThreats(2);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		// With threats to remove and Southrons in play, but in the maneuver phase
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertEquals(2, scn.GetThreats());
		assertFalse(scn.ShadowActionAvailable(horn));

		scn.ShadowPass();
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, explorer2);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertEquals(5, scn.GetStrength(explorer2));
		// Strength 5 + 3 for the Mumakil
		assertEquals(8, scn.GetStrength(explorer));
		assertTrue(scn.ShadowActionAvailable(horn));

		scn.ShadowUseCardAction(horn);
		scn.ShadowChooseCard(explorer2);

		assertEquals(1, scn.GetThreats());
		assertEquals(7, scn.GetStrength(explorer2));
		assertEquals(8, scn.GetStrength(explorer));

		scn.FreepsPass();
		assertTrue(scn.ShadowActionAvailable(horn));

		// The text sets no limit: a second threat buys a second +2.
		scn.ShadowUseCardAction(horn);
		scn.ShadowChooseCard(explorer2);

		assertEquals(0, scn.GetThreats());
		assertEquals(9, scn.GetStrength(explorer2));

		scn.FreepsPass();

		// No threat left to remove
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertFalse(scn.ShadowActionAvailable(horn));

		scn.ShadowPass();

		// Strength 9 beat Aragorn's 8; with the skirmish over, the Explorer is back to his own strength.
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(explorer2));
		assertEquals(5, scn.GetStrength(explorer2));
		// The Mumakil's bearer is fierce and was not assigned, so a fierce assignment phase follows.
		assertEquals(Phase.ASSIGNMENT, scn.GetCurrentPhase());
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void SkirmishAbilityOffersEverySouthronInOrOutOfTheSkirmishAndNoOtherMinion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var horn = scn.GetShadowCard("horn");
		var explorer = scn.GetShadowCard("explorer");
		var explorer2 = scn.GetShadowCard("explorer2");
		var infantry = scn.GetShadowCard("infantry");
		var troop = scn.GetShadowCard("troop");
		var mumakil = scn.GetShadowCard("mumakil");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(explorer, explorer2, infantry, troop);
		scn.AttachCardsTo(explorer, mumakil, horn);
		// Every Southron there is is a [raider] Man: an Orc is made one, to show that the text goes by the keyword.
		scn.ApplyAdHocModifier(new AddKeywordModifier(null, troop, null, Keyword.SOUTHRON));
		scn.AddThreats(1);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, explorer2);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertEquals(1, scn.GetThreats());
		// Strength 5 + 3 for the Mumakil
		assertEquals(8, scn.GetStrength(explorer));
		assertEquals(5, scn.GetStrength(explorer2));
		assertEquals(8, scn.GetStrength(infantry));
		assertTrue(scn.ShadowActionAvailable(horn));

		scn.ShadowUseCardAction(horn);

		// The bearer, mounted and out of the skirmish; the Southron fighting Aragorn, who has no mount; and the
		// Orc who was made a Southron. The Easterling is a [raider] Man and no Southron.
		assertTrue(scn.ShadowHasCardChoiceAvailable(explorer, explorer2, troop));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(infantry));
		assertEquals(1, scn.ShadowGetChoiceMin());

		scn.ShadowChooseCard(explorer);

		assertEquals(0, scn.GetThreats());
		assertEquals(10, scn.GetStrength(explorer));
		assertEquals(5, scn.GetStrength(explorer2));
		assertEquals(8, scn.GetStrength(infantry));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void CanBePlayedOnABladetuskThatHasBeenMadeAMountedSouthronMinion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var horn = scn.GetShadowCard("horn");
		var charger = scn.GetShadowCard("charger");
		var explorer = scn.GetShadowCard("explorer");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCardsToHand(horn);
		scn.MoveCardsToSupportArea(charger);
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(explorer);
		scn.SetTwilight(10);
		scn.FreepsPass();

		// The Charger is still a possession in the support area, and the Explorer has no mount.
		assertEquals(Phase.SHADOW, scn.GetCurrentPhase());
		assertFalse(scn.ShadowPlayAvailable(horn));

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		// The Explorer is the only Southron Man, so he is stacked without a choice.
		scn.ShadowUseCardAction(charger);

		assertInZone(Zone.SHADOW_CHARACTERS, charger);
		assertInZone(Zone.STACKED, explorer);

		// Nobody is assigned, in the ordinary round or the fierce one.
		scn.SkipToAssignments();
		scn.FreepsDeclineAssignments();
		scn.ShadowDeclineAssignments();
		scn.PassFierceAssignmentActions();
		scn.FreepsDeclineAssignments();
		scn.ShadowDeclineAssignments();

		assertEquals(Phase.REGROUP, scn.GetCurrentPhase());
		scn.PassCurrentPhaseActions();
		scn.ShadowDeclineReconciliation();
		scn.FreepsChooseToMove();

		// A second Shadow phase in the turn that made the Charger a minion
		assertEquals(Phase.SHADOW, scn.GetCurrentPhase());
		assertInZone(Zone.SHADOW_CHARACTERS, charger);
		assertTrue(scn.ShadowPlayAvailable(horn));

		scn.ShadowPlayCard(horn);

		assertAttachedTo(horn, charger);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
