package com.gempukku.lotro.cards.official.set04;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_04_028_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("nodefense", "4_28");
					put("nodefense2", "4_28");
					put("brigand", "4_10");     // Dunlending Brigand: [dunland] Man, strength 8, vitality 1, no text
					put("savage", "4_17");      // Dunlending Savage: [dunland] Man, strength 11, vitality 1, no text
					// Easterling Axeman: a [raider] Man, strength 6, vitality 2; strength +3 and fierce only at 2 burdens.
					put("axeman", "4_224");
					// Saruman, Rabble-rouser: a [dunland] minion who is a Wizard and no Man; may not be assigned to a
					// skirmish.  His trigger answers the fellowship's move, so the minions are laid after it; his
					// skirmish ability is used only on purpose.
					put("saruman", "4_33");
					// War Club: [dunland] possession, strength +2, on a [dunland] Man; "Each time bearer wins a
					// skirmish, you may discard 2 Free Peoples possessions" (declined).
					put("warclub", "4_36");
					// Anger: [dunland] Skirmish event, "Spot 3 [dunland] minions or a site you control to make a
					// [dunland] Man fierce until the regroup phase."
					put("anger", "4_3");

					// Gimli, Son of Glóin: strength 6, vitality 3; a skirmish ability used only on purpose.
					put("gimli", "1_13");
					// Dwarven Axe: possession, strength +2, on a Dwarf; its trigger answers a minion losing to him.
					put("dwarvenaxe", "1_9");
					// Ring of Accretion: artifact, strength +1, damage +1, on a Dwarf; a maneuver ability used only on purpose.
					put("accretion", "9_6");
					// Aragorn, Ranger of the North: strength 8; a maneuver ability used only on purpose.
					put("aragorn", "1_89");
					put("sword", "1_112");      // Ranger's Sword: possession, strength +2, damage +1, on Aragorn
					// Elrond, Lord of Rivendell: an ally, strength 8, home site 3.
					put("elrond", "1_40");
					// Elven Sword: possession, strength +1, on an Elf; a skirmish ability used only on purpose.
					put("elvensword", "4_64");

					// Isengard Rider: an [isengard] warg-rider, strength 8, vitality 3; fierce while not exhausted.
					put("rider", "5_53");
					// War-warg: a mount on a warg-rider.  "Response: If a skirmish special ability is used in a
					// skirmish involving bearer, exert bearer to cancel that action."
					put("warwarg", "5_64");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void NoDefenseStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: No Defense
		 * Unique: False
		 * Side: Shadow
		 * Culture: Dunland
		 * Twilight Cost: 0
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: Plays to your support area.<br><b>Shadow:</b> Remove (3) to place a [dunland] token here.<br><b>Skirmish:</b> Spot a [dunland] Man and remove a [dunland] token from this card. A character skirmishing that [dunland] Man does not gain strength bonuses from possessions.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("nodefense");

		assertEquals("No Defense", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.DUNLAND, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void ShadowAbilityRemovesThreeToPlaceATokenOnTheCopyUsed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var nodefense = scn.GetShadowCard("nodefense");
		var nodefense2 = scn.GetShadowCard("nodefense2");
		scn.MoveCardsToSupportArea(nodefense, nodefense2);
		assertEquals(0, scn.GetCultureTokensOn(nodefense));
		assertEquals(0, scn.GetCultureTokensOn(nodefense2));

		scn.StartGame();
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowPhaseActions());

		// One short of the (3) it removes: neither copy is offered.
		scn.SetTwilight(2);
		scn.ShadowRefreshDecision();
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowActionAvailable(nodefense));
		assertFalse(scn.ShadowActionAvailable(nodefense2));

		scn.SetTwilight(3);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowActionAvailable(nodefense));
		assertTrue(scn.ShadowActionAvailable(nodefense2));

		scn.ShadowUseCardAction(nodefense);
		assertEquals(0, scn.GetTwilight());
		// "Here": the token is on the copy used, and not on the other.
		assertEquals(1, scn.GetCultureTokensOn(nodefense));
		assertEquals(0, scn.GetCultureTokensOn(nodefense2));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void SkirmishAbilityStripsPossessionBonusesFromTheCharacterSkirmishingTheSpottedManForThatSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var nodefense = scn.GetShadowCard("nodefense");
		var nodefense2 = scn.GetShadowCard("nodefense2");
		var brigand = scn.GetShadowCard("brigand");
		var savage = scn.GetShadowCard("savage");
		var axeman = scn.GetShadowCard("axeman");
		var saruman = scn.GetShadowCard("saruman");
		var warclub = scn.GetShadowCard("warclub");
		var anger = scn.GetShadowCard("anger");
		var gimli = scn.GetFreepsCard("gimli");
		var dwarvenaxe = scn.GetFreepsCard("dwarvenaxe");
		var accretion = scn.GetFreepsCard("accretion");
		var aragorn = scn.GetFreepsCard("aragorn");
		var sword = scn.GetFreepsCard("sword");
		scn.MoveCompanionsToTable(gimli, aragorn);
		scn.AttachCardsTo(gimli, dwarvenaxe, accretion);
		scn.AttachCardsTo(aragorn, sword);
		scn.MoveCardsToSupportArea(nodefense, nodefense2);
		scn.AddTokensToCard(nodefense, 1);
		scn.AddTokensToCard(nodefense2, 1);
		scn.MoveCardsToHand(anger);
		assertAttachedTo(dwarvenaxe, gimli);
		assertAttachedTo(accretion, gimli);
		assertAttachedTo(sword, aragorn);
		assertEquals(1, scn.GetCultureTokensOn(nodefense));
		assertEquals(1, scn.GetCultureTokensOn(nodefense2));
		assertInHand(anger);

		scn.StartGame();
		scn.FreepsPass();
		// Laid once the fellowship has moved, so that Saruman's move trigger has nothing to answer.
		scn.MoveMinionsToTable(brigand, savage, axeman, saruman);
		scn.AttachCardsTo(brigand, warclub);
		assertAttachedTo(warclub, brigand);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{gimli, brigand}, new PhysicalCardImpl[]{aragorn, savage});
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(gimli);

		// 6 + 2 for the Dwarven Axe + 1 for the Ring of Accretion
		assertEquals(9, scn.GetStrength(gimli));
		// 8 + 2 for the War Club
		assertEquals(10, scn.GetStrength(brigand));
		// 8 + 2 for Ranger's Sword, waiting for his skirmish against the Savage
		assertEquals(10, scn.GetStrength(aragorn));

		scn.FreepsPass();
		assertTrue(scn.ShadowActionAvailable(nodefense2));
		scn.ShadowUseCardAction(nodefense2);
		// "Spot a [dunland] Man": the two in play, whichever skirmish they are in; not a Man of another culture,
		// nor a [dunland] minion who is no Man.
		assertEquals(2, scn.ShadowGetCardChoiceCount());
		assertTrue(scn.ShadowHasCardChoiceAvailable(brigand, savage));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(axeman, saruman));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(savage);
		assertEquals(0, scn.GetCultureTokensOn(nodefense2));
		assertEquals(1, scn.GetCultureTokensOn(nodefense));
		// "That [dunland] Man": nobody is skirmishing the Savage yet, so Gimli keeps his bonuses.
		assertEquals(9, scn.GetStrength(gimli));
		assertEquals(10, scn.GetStrength(aragorn));

		scn.FreepsPass();
		assertTrue(scn.ShadowActionAvailable(nodefense));
		scn.ShadowUseCardAction(nodefense);
		scn.ShadowChooseCard(brigand);
		assertEquals(0, scn.GetCultureTokensOn(nodefense));
		// 6 + 1: the possession's +2 is gone, the artifact's +1 stays.
		assertEquals(7, scn.GetStrength(gimli));
		// The Brigand's own possession, and Aragorn's, who skirmishes nobody yet, are untouched.
		assertEquals(10, scn.GetStrength(brigand));
		assertEquals(10, scn.GetStrength(aragorn));

		// Anger, spotting 3 [dunland] minions, makes the Brigand fierce so that he meets Gimli again.
		scn.FreepsPass();
		scn.ShadowPlayCard(anger);
		scn.ShadowChooseCard(brigand);
		assertTrue(scn.HasKeyword(brigand, Keyword.FIERCE));
		assertEquals(7, scn.GetStrength(gimli));

		// 10 against 7: the Brigand wins; the War Club's offer is declined.
		scn.PassCurrentPhaseActions();
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(warclub));
		scn.ShadowDeclineOptionalTrigger();

		// 11 against 10: Aragorn's skirmish.
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassCurrentPhaseActions();
		assertEquals(1, scn.GetWoundsOn(aragorn));

		// The fierce skirmish against the same Brigand is a phase of its own: Gimli's bonus is back.
		assertTrue(scn.InFierceRound());
		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(gimli, brigand);
		scn.FreepsResolveSkirmish(gimli);
		assertEquals(9, scn.GetStrength(gimli));
		assertEquals(10, scn.GetStrength(brigand));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void AnAllySkirmishingTheSpottedManLosesHisPossessionBonusAndACopyWithNoTokenIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var nodefense = scn.GetShadowCard("nodefense");
		var nodefense2 = scn.GetShadowCard("nodefense2");
		var brigand = scn.GetShadowCard("brigand");
		var elrond = scn.GetFreepsCard("elrond");
		var elvensword = scn.GetFreepsCard("elvensword");
		scn.MoveAlliesToTable(elrond);
		scn.AttachCardsTo(elrond, elvensword);
		scn.MoveCardsToSupportArea(nodefense, nodefense2);
		scn.AddTokensToCard(nodefense, 1);
		assertAttachedTo(elvensword, elrond);
		assertEquals(1, scn.GetCultureTokensOn(nodefense));
		assertEquals(0, scn.GetCultureTokensOn(nodefense2));

		scn.StartGame();
		scn.SkipToSite(2);
		scn.MoveMinionsToTable(brigand);
		// The fellowship moves to site 3, Elrond's home, where he may be assigned to a skirmish.
		scn.SkipToAssignments();
		assertEquals(3, scn.GetCurrentSiteNumber());
		scn.FreepsAssignToMinions(elrond, brigand);
		scn.FreepsResolveSkirmish(elrond);
		// 8 + 1 for the Elven Sword
		assertEquals(9, scn.GetStrength(elrond));

		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		// "From this card": the copy with no token is not offered, the one with a token is.
		assertFalse(scn.ShadowActionAvailable(nodefense2));
		assertTrue(scn.ShadowActionAvailable(nodefense));

		// The one [dunland] Man is spotted unasked.
		scn.ShadowUseCardAction(nodefense);
		assertEquals(0, scn.GetCultureTokensOn(nodefense));
		assertEquals(8, scn.GetStrength(elrond));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void SkirmishAbilityIsNotOfferedWithNoDunlandManToSpot() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var nodefense = scn.GetShadowCard("nodefense");
		var axeman = scn.GetShadowCard("axeman");
		var saruman = scn.GetShadowCard("saruman");
		var gimli = scn.GetFreepsCard("gimli");
		var dwarvenaxe = scn.GetFreepsCard("dwarvenaxe");
		scn.MoveCompanionsToTable(gimli);
		scn.AttachCardsTo(gimli, dwarvenaxe);
		scn.MoveCardsToSupportArea(nodefense);
		scn.AddTokensToCard(nodefense, 1);
		assertAttachedTo(dwarvenaxe, gimli);
		assertEquals(1, scn.GetCultureTokensOn(nodefense));

		scn.StartGame();
		scn.FreepsPass();
		// Laid once the fellowship has moved, so that Saruman's move trigger has nothing to answer.
		scn.MoveMinionsToTable(axeman, saruman);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gimli, axeman);
		scn.FreepsResolveSkirmish(gimli);

		// A Man of another culture in the skirmish and a [dunland] minion who is no Man on the table: nothing
		// to spot, so the token stays and the ability is not offered.
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertFalse(scn.ShadowActionAvailable(nodefense));
		assertEquals(1, scn.GetCultureTokensOn(nodefense));
		// 6 + 2 for the Dwarven Axe
		assertEquals(8, scn.GetStrength(gimli));
	}
	@Test
	public void CancelledByAWarWargTheTokenStaysRemovedAndTheCharacterKeepsHisPossessionBonus() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var nodefense = scn.GetShadowCard("nodefense");
		var brigand = scn.GetShadowCard("brigand");
		var rider = scn.GetShadowCard("rider");
		var warwarg = scn.GetShadowCard("warwarg");
		var gimli = scn.GetFreepsCard("gimli");
		var dwarvenaxe = scn.GetFreepsCard("dwarvenaxe");
		scn.MoveCompanionsToTable(gimli);
		scn.AttachCardsTo(gimli, dwarvenaxe);
		scn.MoveCardsToSupportArea(nodefense);
		scn.AddTokensToCard(nodefense, 1);
		assertAttachedTo(dwarvenaxe, gimli);
		assertEquals(1, scn.GetCultureTokensOn(nodefense));

		scn.StartGame();
		scn.FreepsPass();
		scn.MoveMinionsToTable(brigand, rider);
		scn.AttachCardsTo(rider, warwarg);
		assertAttachedTo(warwarg, rider);

		// The Brigand and the War-warg's bearer in one skirmish against Gimli: the Shadow player assigns the one
		// left over.
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gimli, brigand);
		scn.ShadowAssignToMinions(gimli, rider);
		scn.FreepsResolveSkirmish(gimli);
		// 6 + 2 for the Dwarven Axe
		assertEquals(8, scn.GetStrength(gimli));

		scn.FreepsPass();
		assertTrue(scn.ShadowActionAvailable(nodefense));
		// The one [dunland] Man is spotted unasked.
		scn.ShadowUseCardAction(nodefense);
		assertEquals(0, scn.GetCultureTokensOn(nodefense));

		// "Cancel that action": the first sentence is the price, paid; the second is what it buys, and is not
		// carried out.
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(warwarg));
		scn.ShadowAcceptOptionalTrigger();
		assertEquals(1, scn.GetWoundsOn(rider));
		assertEquals(0, scn.GetCultureTokensOn(nodefense));
		assertEquals(8, scn.GetStrength(gimli));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}
}
