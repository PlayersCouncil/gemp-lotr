package com.gempukku.lotro.cards.unofficial.rtmd.set_94_pc;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_94_004_Tests
{
	protected VirtualTableScenario GetFreepsScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a [gondor] Man, vitality 4; the dying companion, bears the possession
					put("gimli", "1_13");       // Gimli, Son of Gloin: a Dwarf companion; eligible alternate bearer of the cloak
					put("knight", "5_35");      // Gondorian Knight: a [gondor] Man companion, vitality 3; Knight is its whole text
					put("erland", "2_21");      // Erland, Advisor to Brand: a [gandalf] Man ally, vitality 2
					put("warrior", "17_104");   // Warrior of Rohan: a [rohan] Man, vitality 3; "Possessions cannot be transferred from this companion to another character."
					put("cloak", "1_42");       // Elven Cloak: a possession (cloak); "Bearer must be a companion."
					put("armor", "1_92");       // Armor: a possession (armor); "Bearer must be a Man."
					put("armor2", "1_92");
					put("rsword", "1_112");     // Ranger's Sword: a possession; "Bearer must be Aragorn."
					put("barahir", "6_55");     // Ring of Barahir: an artifact; "Bearer must be Aragorn."
					put("bow", "7_94");         // Gondor Bow: a possession (ranged weapon); "Bearer must be a [gondor] Man."; its Skirmish ability is never used
					put("anduril", "7_79");     // Anduril, Flame of the West: an artifact (hand weapon); "Bearer must be Aragorn. ... He ... cannot bear other weapons."; its Regroup ability is never used
					put("slayer", "3_93");      // Morgul Slayer: an Orc minion, vitality 2; "Regroup: Exert this minion to wound a companion (except the Ring-bearer)."
					put("slayer2", "3_93");
					put("brigand", "4_10");     // Dunlending Brigand: a Man minion with no text
					put("assassin", "2_93");    // Tower Assassin: a minion, vitality 3; "Maneuver: Exert Tower Assassin to wound an ally."
					put("despair", "7_148");    // Fierce in Despair: a Shadow Regroup event, twilight 7: "Spot 2 [raider] Men to discard a companion (except the Ring-bearer)."
					put("axeman1", "4_224");    // Easterling Axeman: a [raider] Man minion; his text needs 2 burdens
					put("axeman2", "4_224");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				"94_4", null
		);
	}

	protected VirtualTableScenario GetShadowScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a [gondor] Man, vitality 4; the dying companion, bears the possession
					put("gimli", "1_13");       // Gimli, Son of Gloin: a Dwarf companion; eligible alternate bearer of the cloak
					put("cloak", "1_42");       // Elven Cloak: a possession (cloak); "Bearer must be a companion."
					put("slayer", "3_93");      // Morgul Slayer: an Orc minion, vitality 2; "Regroup: Exert this minion to wound a companion (except the Ring-bearer)."
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				null, "94_4"
		);
	}

	@Test
	public void StatsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {
		/**
		 * Set: RTMD 94
		 * Name: Race Text 94_4
		 * Type: MetaSite
		 * Intensity: -2
		 * Game Text: Each time your companion is killed, you may transfer each possession attached to that
		 * companion to another eligible bearer.
		 */

		var scn = GetFreepsScenario();

		var mod = scn.GetFreepsCard("mod");

		assertEquals("Race Text 94_4", mod.getBlueprint().getTitle());
		assertEquals(CardType.METASITE, mod.getBlueprint().getCardType());
		assertEquals(-2, mod.getBlueprint().getIntensity());
	}

	@Test
	public void OwnerMayTransferThePossessionBeforeTheCompanionDies() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario();
		var mod = scn.GetFreepsCard("mod");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var cloak = scn.GetFreepsCard("cloak");

		scn.MoveCompanionsToTable(aragorn, gimli);
		scn.AttachCardsTo(aragorn, cloak);
		scn.StartGame();

		scn.AddWoundsToChar(aragorn, aragorn.getBlueprint().getVitality());
		// pump the loop just once so the game stops at the offered optional trigger rather than
		// blindly declining everything in transit
		scn.FreepsPass();

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(mod));
		scn.FreepsAcceptOptionalTrigger();
		// Only one possession is attached, so the "choose which possession" step auto-resolves and
		// the first decision presented is the destination bearer.
		scn.FreepsChooseCard(gimli);

		assertEquals(gimli, cloak.getAttachedTo());
		assertInZone(Zone.DEAD, aragorn);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void DecliningLeavesThePossessionToBeDiscardedWithTheCompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario();
		var mod = scn.GetFreepsCard("mod");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var cloak = scn.GetFreepsCard("cloak");

		scn.MoveCompanionsToTable(aragorn, gimli);
		scn.AttachCardsTo(aragorn, cloak);
		scn.StartGame();

		scn.AddWoundsToChar(aragorn, aragorn.getBlueprint().getVitality());
		scn.FreepsPass();

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(mod));
		scn.FreepsDeclineOptionalTrigger();

		assertInZone(Zone.DEAD, aragorn);
		assertNotEquals(gimli, cloak.getAttachedTo());
		assertInZone(Zone.DISCARD, cloak);
		// The offer was the owner's alone: the other player is not asked in his place.
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void NotOfferedToNonOwner() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario();
		var mod = scn.GetFreepsCard("mod");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var cloak = scn.GetFreepsCard("cloak");

		scn.MoveCompanionsToTable(aragorn, gimli);
		scn.AttachCardsTo(aragorn, cloak);
		scn.StartGame();

		scn.AddWoundsToChar(aragorn, aragorn.getBlueprint().getVitality());
		scn.FreepsPass();

		// The card's owner (Freeps) is offered the trigger; the Shadow player, who does not own this
		// copy of the metasite, is not - even though "your" would otherwise resolve against whichever
		// player is asked.
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(mod));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(mod));
	}

	@Test
	public void EachPossessionGoesToABearerItsOwnTextAllowsAndNothingElseMoves() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario();

		var mod = scn.GetFreepsCard("mod");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var knight = scn.GetFreepsCard("knight");
		var erland = scn.GetFreepsCard("erland");
		var frodo = scn.GetRingBearer();
		var cloak = scn.GetFreepsCard("cloak");
		var armor = scn.GetFreepsCard("armor");
		var knightsArmor = scn.GetFreepsCard("armor2");
		var barahir = scn.GetFreepsCard("barahir");
		var slayer = scn.GetShadowCard("slayer");
		var brigand = scn.GetShadowCard("brigand");

		scn.MoveCompanionsToTable(aragorn, gimli, knight);
		scn.MoveAlliesToTable(erland);
		scn.AttachCardsTo(aragorn, cloak, armor);
		scn.AttachCardsTo(knight, knightsArmor);

		scn.StartGame();
		// The Ring's own offer comes at the start of a turn: it goes on after that.
		scn.AttachCardsTo(aragorn, barahir);
		scn.MoveMinionsToTable(slayer, brigand);
		// Vitality 4 with 3 wounds: the Slayer's wound kills him.
		scn.AddWoundsToChar(aragorn, 3);
		scn.SkipToPhase(Phase.REGROUP);

		scn.FreepsPass();
		scn.ShadowUseCardAction(slayer);
		scn.ShadowChooseCard(aragorn);

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(mod));
		assertAttachedTo(cloak, aragorn);
		assertAttachedTo(armor, aragorn);
		int twilight = scn.GetTwilight();
		scn.FreepsAcceptOptionalTrigger();

		// His two possessions, and not his artifact or the Knight's own Armor
		assertTrue(scn.FreepsHasCardChoiceAvailable(cloak, armor));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(barahir, knightsArmor));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(armor);

		// "Bearer must be a Man": a Man who is an ally and a Man who is the other player's minion.
		// Not the Knight, a Man already in Armor; not a Dwarf, a Hobbit or an Orc; not Aragorn again.
		assertTrue(scn.FreepsHasCardChoiceAvailable(erland, brigand));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(knight, gimli, frodo, slayer, aragorn));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(erland);
		assertAttachedTo(armor, erland);

		// The Cloak is the one possession left on him. "Bearer must be a companion": any of the other three.
		assertTrue(scn.FreepsHasCardChoiceAvailable(gimli, knight, frodo));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(erland, brigand, slayer, aragorn));
		scn.FreepsChooseCard(gimli);

		assertAttachedTo(cloak, gimli);
		assertAttachedTo(armor, erland);
		assertAttachedTo(knightsArmor, knight);
		assertInZone(Zone.DEAD, aragorn);
		assertInZone(Zone.DISCARD, barahir);
		// A transfer in the fellowship phase would have cost 1 for each: this one costs nothing.
		assertEquals(twilight, scn.GetTwilight());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void ASecondCompanionKilledInTheTurnIsOfferedAgain() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario();

		var mod = scn.GetFreepsCard("mod");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var knight = scn.GetFreepsCard("knight");
		var frodo = scn.GetRingBearer();
		var cloak = scn.GetFreepsCard("cloak");
		var armor = scn.GetFreepsCard("armor");
		var slayer = scn.GetShadowCard("slayer");
		var slayer2 = scn.GetShadowCard("slayer2");
		var brigand = scn.GetShadowCard("brigand");

		scn.MoveCompanionsToTable(aragorn, gimli, knight);
		scn.AttachCardsTo(aragorn, armor);
		scn.AttachCardsTo(gimli, cloak);

		scn.StartGame();
		scn.MoveMinionsToTable(slayer, slayer2, brigand);
		// One wound short of dead, each of them: Aragorn has 4 vitality and Gimli 3.
		scn.AddWoundsToChar(aragorn, 3);
		scn.AddWoundsToChar(gimli, 2);
		scn.SkipToPhase(Phase.REGROUP);

		scn.FreepsPass();
		scn.ShadowUseCardAction(slayer);
		scn.ShadowChooseCard(aragorn);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(mod));
		scn.FreepsAcceptOptionalTrigger();
		// The Armor may go to either of the other Men, the other player's among them.
		assertTrue(scn.FreepsHasCardChoiceAvailable(knight, brigand));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(gimli, frodo));
		scn.FreepsChooseCard(brigand);
		assertAttachedTo(armor, brigand);
		assertInZone(Zone.DEAD, aragorn);

		scn.FreepsPass();
		scn.ShadowUseCardAction(slayer2);
		scn.ShadowChooseCard(gimli);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(mod));
		assertAttachedTo(cloak, gimli);
		scn.FreepsAcceptOptionalTrigger();
		assertTrue(scn.FreepsHasCardChoiceAvailable(knight, frodo));
		scn.FreepsChooseCard(knight);

		assertAttachedTo(cloak, knight);
		assertAttachedTo(armor, brigand);
		assertInZone(Zone.DEAD, gimli);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void AnAllyKilledWithAPossessionIsNoCompanionAndNothingIsOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario();

		var erland = scn.GetFreepsCard("erland");
		var knight = scn.GetFreepsCard("knight");
		var armor = scn.GetFreepsCard("armor");
		var assassin = scn.GetShadowCard("assassin");

		scn.MoveCompanionsToTable(knight);
		scn.MoveAlliesToTable(erland);
		scn.AttachCardsTo(erland, armor);

		scn.StartGame();
		scn.MoveMinionsToTable(assassin);
		scn.SkipToPhase(Phase.MANEUVER);

		// Erland is the only ally and has 2 vitality: the Assassin's two wounds are his.
		// The Knight, a Man with no armor, could bear what Erland bears.
		scn.FreepsPass();
		scn.ShadowUseCardAction(assassin);
		assertEquals(1, scn.GetWoundsOn(erland));
		assertAttachedTo(armor, erland);
		scn.FreepsPass();
		scn.ShadowUseCardAction(assassin);

		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertInZone(Zone.DEAD, erland);
		assertInZone(Zone.DISCARD, armor);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void ACompanionDiscardedFromPlayIsNotKilledAndNothingIsOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var cloak = scn.GetFreepsCard("cloak");
		var despair = scn.GetShadowCard("despair");
		var axeman1 = scn.GetShadowCard("axeman1");
		var axeman2 = scn.GetShadowCard("axeman2");

		scn.MoveCompanionsToTable(aragorn, gimli);
		scn.AttachCardsTo(aragorn, cloak);

		scn.StartGame();
		scn.MoveMinionsToTable(axeman1, axeman2);
		scn.MoveCardsToHand(despair);
		scn.SkipToPhase(Phase.REGROUP);
		// Fierce in Despair costs 7.
		scn.SetTwilight(7);

		scn.FreepsPass();
		scn.ShadowPlayCard(despair);
		assertTrue(scn.ShadowHasCardChoiceAvailable(aragorn, gimli));
		assertAttachedTo(cloak, aragorn);
		scn.ShadowChooseCard(aragorn);

		// Discarded from play is not killed (CR 4.2 "killed"): the Cloak goes with him, though Gimli could bear it.
		assertInZone(Zone.DISCARD, aragorn);
		assertEquals(0, scn.GetFreepsDeadCount());
		assertInZone(Zone.DISCARD, cloak);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void TheOwnerAsPlayerTwoIsOfferedOnHisOwnTurnAndPlayerOneIsNot() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetShadowScenario();

		var mod = scn.GetShadowCard("mod");
		var aragorn = scn.GetShadowCard("aragorn");
		var gimli = scn.GetShadowCard("gimli");
		var cloak = scn.GetShadowCard("cloak");
		var slayer = scn.GetFreepsCard("slayer");

		scn.StartGame();
		// Player 2's turn: he is the Free Peoples player, and the modifier is his.
		scn.SkipToOtherPlayersTurn();
		scn.ShadowPass();
		scn.MoveCompanionsToTable(aragorn, gimli);
		scn.AttachCardsTo(aragorn, cloak);
		scn.MoveMinionsToTable(slayer);
		// Vitality 4 with 3 wounds: the Slayer's wound kills him.
		scn.AddWoundsToChar(aragorn, 3);
		scn.SkipToPhaseInverted(Phase.REGROUP);

		scn.ShadowPass();
		scn.FreepsUseCardAction(slayer);
		scn.FreepsChooseCard(aragorn);

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(mod));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertAttachedTo(cloak, aragorn);
		scn.ShadowAcceptOptionalTrigger();
		assertTrue(scn.ShadowHasCardChoiceAvailable(gimli));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(aragorn));
		scn.ShadowChooseCard(gimli);

		assertAttachedTo(cloak, gimli);
		assertInZone(Zone.DEAD, aragorn);
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void NotOfferedWhenTheCompanionBearsAnArtifactAndNoPossession() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var barahir = scn.GetFreepsCard("barahir");
		var slayer = scn.GetShadowCard("slayer");

		scn.MoveCompanionsToTable(aragorn, gimli);

		scn.StartGame();
		scn.AttachCardsTo(aragorn, barahir);
		scn.MoveMinionsToTable(slayer);
		scn.AddWoundsToChar(aragorn, 3);
		scn.SkipToPhase(Phase.REGROUP);

		scn.FreepsPass();
		scn.ShadowUseCardAction(slayer);
		scn.ShadowChooseCard(aragorn);

		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertInZone(Zone.DEAD, aragorn);
		assertInZone(Zone.DISCARD, barahir);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void NotOfferedWhenNobodyElseMayBearTheOnlyPossession() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var rsword = scn.GetFreepsCard("rsword");
		var slayer = scn.GetShadowCard("slayer");

		scn.MoveCompanionsToTable(aragorn, gimli);
		scn.AttachCardsTo(aragorn, rsword);

		scn.StartGame();
		scn.MoveMinionsToTable(slayer);
		scn.AddWoundsToChar(aragorn, 3);
		scn.SkipToPhase(Phase.REGROUP);

		scn.FreepsPass();
		scn.ShadowUseCardAction(slayer);
		scn.ShadowChooseCard(aragorn);

		// "Bearer must be Aragorn": Ranger's Sword has nowhere else to go.
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertInZone(Zone.DEAD, aragorn);
		assertInZone(Zone.DISCARD, rsword);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void NotOfferedWhenThePossessionCannotBeTransferredFromTheCompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario();

		var warrior = scn.GetFreepsCard("warrior");
		var gimli = scn.GetFreepsCard("gimli");
		var cloak = scn.GetFreepsCard("cloak");
		var slayer = scn.GetShadowCard("slayer");

		scn.MoveCompanionsToTable(warrior, gimli);
		scn.AttachCardsTo(warrior, cloak);

		scn.StartGame();
		scn.MoveMinionsToTable(slayer);
		// Vitality 3 with 2 wounds: the Slayer's wound kills him.
		scn.AddWoundsToChar(warrior, 2);
		scn.SkipToPhase(Phase.REGROUP);

		scn.FreepsPass();
		scn.ShadowUseCardAction(slayer);
		scn.ShadowChooseCard(warrior);

		// Gimli and Frodo could bear the Cloak, and by the Warrior's own text it cannot be transferred from him.
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertInZone(Zone.DEAD, warrior);
		assertInZone(Zone.DISCARD, cloak);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void NotOfferedWhenTheOnlyOtherBearerItsTextAllowsAlreadyBearsAnItemOfItsClass() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var knight = scn.GetFreepsCard("knight");
		var gimli = scn.GetFreepsCard("gimli");
		var armor = scn.GetFreepsCard("armor");
		var knightsArmor = scn.GetFreepsCard("armor2");
		var slayer = scn.GetShadowCard("slayer");

		scn.MoveCompanionsToTable(aragorn, knight, gimli);
		scn.AttachCardsTo(aragorn, armor);
		scn.AttachCardsTo(knight, knightsArmor);

		scn.StartGame();
		scn.MoveMinionsToTable(slayer);
		scn.AddWoundsToChar(aragorn, 3);
		scn.SkipToPhase(Phase.REGROUP);

		scn.FreepsPass();
		scn.ShadowUseCardAction(slayer);
		scn.ShadowChooseCard(aragorn);

		// "Bearer must be a Man": the Knight is the only other Man, and a character bears one armor (CR 4.2
		// "bearer").  Gimli and Frodo are no Men.
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertInZone(Zone.DEAD, aragorn);
		assertInZone(Zone.DISCARD, armor);
		assertAttachedTo(knightsArmor, knight);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void NotOfferedWhenTheOnlyOtherBearerItsTextAllowsIsKeptFromBearingItByAnotherCard() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario();

		var knight = scn.GetFreepsCard("knight");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var bow = scn.GetFreepsCard("bow");
		var anduril = scn.GetFreepsCard("anduril");
		var slayer = scn.GetShadowCard("slayer");

		scn.MoveCompanionsToTable(knight, aragorn, gimli);
		scn.AttachCardsTo(knight, bow);
		scn.AttachCardsTo(aragorn, anduril);

		scn.StartGame();
		scn.MoveMinionsToTable(slayer);
		// Vitality 3 with 2 wounds: the Slayer's wound kills him.
		scn.AddWoundsToChar(knight, 2);
		scn.SkipToPhase(Phase.REGROUP);

		scn.FreepsPass();
		scn.ShadowUseCardAction(slayer);
		scn.ShadowChooseCard(knight);

		// "Bearer must be a [gondor] Man": Aragorn is the only other one, and Anduril's "cannot bear other weapons"
		// keeps the Bow from him.
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertInZone(Zone.DEAD, knight);
		assertInZone(Zone.DISCARD, bow);
		assertAttachedTo(anduril, aragorn);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void NotOfferedWhileTheOnlyOtherBearerItsTextAllowsIsHindered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var knight = scn.GetFreepsCard("knight");
		var gimli = scn.GetFreepsCard("gimli");
		var armor = scn.GetFreepsCard("armor");
		var slayer = scn.GetShadowCard("slayer");

		scn.MoveCompanionsToTable(aragorn, knight, gimli);
		scn.AttachCardsTo(aragorn, armor);

		scn.StartGame();
		scn.MoveMinionsToTable(slayer);
		scn.AddWoundsToChar(aragorn, 3);
		scn.SkipToPhase(Phase.REGROUP);
		// The Knight, the only other Man, is hindered by the rig (CR 5.0 "hinder").
		scn.HinderCard(knight);

		scn.FreepsPass();
		scn.ShadowUseCardAction(slayer);
		scn.ShadowChooseCard(aragorn);

		assertTrue(scn.IsHindered(knight));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertInZone(Zone.DEAD, aragorn);
		assertInZone(Zone.DISCARD, armor);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	// The time limit is for F-0322: before it was fixed the game never came back from the last answer,
	// and a test that hangs stops every test after it.
	@Test(timeout = 20000)
	public void APossessionNobodyElseMayBearIsDiscardedWithTheCompanionOnceTheOthersHaveMoved() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario();

		var mod = scn.GetFreepsCard("mod");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var frodo = scn.GetRingBearer();
		var cloak = scn.GetFreepsCard("cloak");
		var rsword = scn.GetFreepsCard("rsword");
		var slayer = scn.GetShadowCard("slayer");

		scn.MoveCompanionsToTable(aragorn, gimli);
		scn.AttachCardsTo(aragorn, cloak, rsword);

		scn.StartGame();
		scn.MoveMinionsToTable(slayer);
		scn.AddWoundsToChar(aragorn, 3);
		scn.SkipToPhase(Phase.REGROUP);

		scn.FreepsPass();
		scn.ShadowUseCardAction(slayer);
		scn.ShadowChooseCard(aragorn);

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(mod));
		scn.FreepsAcceptOptionalTrigger();

		// The Cloak is the only possession that can go anywhere; Ranger's Sword is Aragorn's alone.
		assertTrue(scn.FreepsHasCardChoiceAvailable(gimli, frodo));
		scn.FreepsChooseCard(gimli);

		assertAttachedTo(cloak, gimli);
		assertInZone(Zone.DEAD, aragorn);
		assertInZone(Zone.DISCARD, rsword);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void ScopingWhenShadowOwnsModFreepsCompanionGetsNoOffer() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetShadowScenario();
		var mod = scn.GetShadowCard("mod");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var cloak = scn.GetFreepsCard("cloak");

		scn.MoveCompanionsToTable(aragorn, gimli);
		scn.AttachCardsTo(aragorn, cloak);
		scn.StartGame();

		scn.AddWoundsToChar(aragorn, aragorn.getBlueprint().getVitality());
		scn.FreepsPass();

		// "your,companion" combined with IsOwner: Shadow owns the mod, so Freeps' own companion
		// dying does not trigger it.
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(mod));
		assertInZone(Zone.DEAD, aragorn);
		// Nor is the owner offered it for a companion who is not his: the Cloak is gone with its bearer.
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertInZone(Zone.DISCARD, cloak);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
