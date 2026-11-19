package com.gempukku.lotro.cards.official.set17;

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

public class Card_17_113_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("roar", "17_113");
					put("roar2", "17_113");

					put("feral", "11_183");      // Feral Uruk: [uruk-hai] Uruk-hai, strength 9, vitality 2; its text only at a battleground, and this path has none
					put("saruman", "17_116");    // Saruman, Master of the White Hand: an [uruk-hai] minion who is no Uruk-hai, strength 8; his ability used only on purpose
					put("raiders", "1_158");     // Uruk-hai Raiding Party: an [isengard] Uruk-hai, strength 9, damage +1 and nothing else
					put("captain", "17_123");    // White Hand Captain: [uruk-hai] Uruk-hai, fierce, strength 12; its +1 is for hunters, and none is laid
					put("sword", "11_192");      // Isengard Sword: a Shadow possession for an [uruk-hai] minion, cost 1
					put("fortitude", "11_185");  // Fortitude: another [uruk-hai] card that carries [uruk-hai] tokens; its Response answers wounds to [uruk-hai] minions only

					put("gimli", "1_13");        // Gimli, Son of Gloin: Dwarf, strength 6, vitality 3; his ability used only on purpose
					put("armor", "1_8");         // Dwarven Armor: a Free Peoples possession for a Dwarf, cost 0, which changes no number
					put("artifice", "17_4");     // Ring of Artifice: a Free Peoples artifact for a Dwarf, cost 0
					put("nostranger", "1_108");  // No Stranger to the Shadows: a Free Peoples condition for a ranger, cost 0
					put("aragorn", "1_89");      // Aragorn, Ranger of the North: strength 8, vitality 4; his ability used only on purpose
					put("guard", "1_7");         // Dwarf Guard: strength 4, vitality 2
					put("barliman", "1_70");     // Barliman Butterbur: an ally, vitality 2; his ability used only on purpose
					put("found", "18_2");        // Run Until Found: a [dwarven] card that carries [dwarven] tokens
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void DeathlyRoarStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 17
		 * Name: Deathly Roar
		 * Unique: False
		 * Side: Shadow
		 * Culture: Uruk-hai
		 * Twilight Cost: 2
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: Each time the Free Peoples player plays a possession or artifact, place an [uruk-hai] token here.
		 * Each time an [uruk-hai] minion wins a skirmish, you may remove an [uruk-hai] token to exert a companion.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("roar");

		assertEquals("Deathly Roar", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.URUK_HAI, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void EachCopyTakesATokenForEachPossessionOrArtifactTheFreePeoplesPlayerPlaysAndNoneForACondition() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var roar = scn.GetShadowCard("roar");
		var roar2 = scn.GetShadowCard("roar2");
		var gimli = scn.GetFreepsCard("gimli");
		var aragorn = scn.GetFreepsCard("aragorn");
		var armor = scn.GetFreepsCard("armor");
		var artifice = scn.GetFreepsCard("artifice");
		var nostranger = scn.GetFreepsCard("nostranger");
		scn.MoveCompanionsToTable(gimli, aragorn);
		scn.MoveCardsToSupportArea(roar, roar2);
		scn.MoveCardsToHand(armor, artifice, nostranger);
		assertInHand(armor, artifice, nostranger);

		scn.StartGame();
		assertEquals(0, scn.GetCultureTokensOn(roar));
		assertEquals(0, scn.GetCultureTokensOn(roar2));

		scn.FreepsPlayCard(armor);
		assertAttachedTo(armor, gimli);
		assertEquals(1, scn.GetCultureTokensOn(roar));
		assertEquals(1, scn.GetCultureTokensOn(roar2));

		scn.FreepsPlayCard(artifice);
		assertAttachedTo(artifice, gimli);
		assertEquals(2, scn.GetCultureTokensOn(roar));
		assertEquals(2, scn.GetCultureTokensOn(roar2));

		// A condition is neither.
		scn.FreepsPlayCard(nostranger);
		assertAttachedTo(nostranger, aragorn);
		assertEquals(2, scn.GetCultureTokensOn(roar));
		assertEquals(2, scn.GetCultureTokensOn(roar2));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void APossessionOrADeathlyRoarPlayedByTheShadowPlayerPlacesNoToken() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var roar = scn.GetShadowCard("roar");
		var roar2 = scn.GetShadowCard("roar2");
		var feral = scn.GetShadowCard("feral");
		var sword = scn.GetShadowCard("sword");
		scn.MoveCardsToSupportArea(roar);
		scn.MoveCardsToHand(sword, roar2);
		assertInHand(sword, roar2);

		scn.StartGame();
		scn.MoveMinionsToTable(feral);
		scn.SetTwilight(5);
		scn.FreepsPass();

		assertEquals(0, scn.GetCultureTokensOn(roar));
		// The second copy, played from hand: a Shadow condition, and so no possession or artifact.
		scn.ShadowPlayCard(roar2);
		assertInZone(Zone.SUPPORT, roar2);
		assertEquals(0, scn.GetCultureTokensOn(roar));
		assertEquals(0, scn.GetCultureTokensOn(roar2));

		scn.ShadowPlayCard(sword);
		assertAttachedTo(sword, feral);
		assertEquals(0, scn.GetCultureTokensOn(roar));
		assertEquals(0, scn.GetCultureTokensOn(roar2));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WhenAnUrukHaiMinionWinsTheTokenIsRemovedToExertACompanionOfTheShadowPlayersChoice() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var roar = scn.GetShadowCard("roar");
		var feral = scn.GetShadowCard("feral");
		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var guard = scn.GetFreepsCard("guard");
		var barliman = scn.GetFreepsCard("barliman");
		scn.MoveCompanionsToTable(aragorn, gimli, guard);
		scn.MoveAlliesToTable(barliman);
		scn.MoveCardsToSupportArea(roar);
		scn.AddTokensToCard(roar, 1);
		// Vitality 2 with 1 wound: exhausted.
		scn.AddWoundsToChar(guard, 1);
		assertEquals(1, scn.GetCultureTokensOn(roar));
		assertEquals(1, scn.GetWoundsOn(guard));

		scn.StartGame();
		scn.MoveMinionsToTable(feral);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, feral);
		scn.FreepsResolveSkirmish(aragorn);
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(frodo));

		// 9 against 8: the Uruk wins.
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(roar));
		scn.ShadowChooseOptionalTrigger(roar);

		assertEquals(0, scn.GetCultureTokensOn(roar));
		// Any companion who can exert, the loser too; not the exhausted Dwarf Guard, the ally, or a minion.
		assertTrue(scn.ShadowHasCardChoiceAvailable(frodo, aragorn, gimli));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(guard, barliman, feral));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(gimli);

		assertEquals(1, scn.GetWoundsOn(gimli));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetWoundsOn(guard));
		assertEquals(0, scn.GetWoundsOn(barliman));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void AnUrukHaiMinionWhoIsNoUrukHaiWinningIsAnsweredAndAnIsengardUrukHaiIsNotAndDecliningChangesNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var roar = scn.GetShadowCard("roar");
		var saruman = scn.GetShadowCard("saruman");
		var raiders = scn.GetShadowCard("raiders");
		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(aragorn, gimli);
		scn.MoveCardsToSupportArea(roar);
		scn.AddTokensToCard(roar, 1);
		assertEquals(1, scn.GetCultureTokensOn(roar));

		scn.StartGame();
		scn.MoveMinionsToTable(saruman, raiders);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, raiders}, new PhysicalCardImpl[]{gimli, saruman});

		// The [isengard] Uruk-hai beats Aragorn, 9 against 8, with damage +1.
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(roar));
		assertEquals(1, scn.GetCultureTokensOn(roar));

		// Saruman, an [uruk-hai] Wizard, beats Gimli, 8 against 6.
		scn.FreepsResolveSkirmish(gimli);
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(roar));
		scn.ShadowDeclineOptionalTrigger();

		assertEquals(1, scn.GetCultureTokensOn(roar));
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void AnUrukHaiMinionLosingASkirmishIsNotAnswered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var roar = scn.GetShadowCard("roar");
		var feral = scn.GetShadowCard("feral");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToSupportArea(roar);
		scn.AddTokensToCard(roar, 1);
		assertEquals(1, scn.GetCultureTokensOn(roar));

		scn.StartGame();
		scn.MoveMinionsToTable(feral);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, feral);
		scn.FreepsResolveSkirmish(aragorn);
		// 13 against 9: Aragorn wins, short of double, and the Uruk lives with 1 wound.
		scn.ApplyAdHocModifier(new StrengthModifier(null, aragorn, null, 5));
		scn.PassSkirmishActions();

		assertEquals(1, scn.GetWoundsOn(feral));
		assertInZone(Zone.SHADOW_CHARACTERS, feral);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(roar));
		assertEquals(1, scn.GetCultureTokensOn(roar));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WithNoUrukHaiTokenInPlayTheWinIsNotAnsweredThoughATokenOfAnotherCultureIsThere() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var roar = scn.GetShadowCard("roar");
		var feral = scn.GetShadowCard("feral");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var found = scn.GetFreepsCard("found");
		scn.MoveCompanionsToTable(aragorn, gimli);
		scn.MoveCardsToSupportArea(roar, found);
		scn.AddTokensToCard(found, 1);
		assertEquals(0, scn.GetCultureTokensOn(roar));
		assertEquals(1, scn.GetCultureTokensOn(found));

		scn.StartGame();
		scn.MoveMinionsToTable(feral);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, feral);
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();

		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(roar));
		assertEquals(1, scn.GetCultureTokensOn(found));
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void TheTokenMayBeRemovedFromAnotherUrukHaiCard() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var roar = scn.GetShadowCard("roar");
		var fortitude = scn.GetShadowCard("fortitude");
		var feral = scn.GetShadowCard("feral");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(aragorn, gimli);
		scn.MoveCardsToSupportArea(roar, fortitude);
		scn.AddTokensToCard(fortitude, 1);
		assertEquals(0, scn.GetCultureTokensOn(roar));
		assertEquals(1, scn.GetCultureTokensOn(fortitude));

		scn.StartGame();
		scn.MoveMinionsToTable(feral);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, feral);
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(roar));
		scn.ShadowChooseOptionalTrigger(roar);
		assertEquals(0, scn.GetCultureTokensOn(fortitude));
		scn.ShadowChooseCard(gimli);
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void AFierceUrukHaiMinionWinningTwiceIsAnsweredEachTime() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var roar = scn.GetShadowCard("roar");
		var captain = scn.GetShadowCard("captain");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(aragorn, gimli);
		scn.MoveCardsToSupportArea(roar);
		scn.AddTokensToCard(roar, 2);
		assertEquals(2, scn.GetCultureTokensOn(roar));

		scn.StartGame();
		scn.MoveMinionsToTable(captain);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, captain);
		scn.FreepsResolveSkirmish(aragorn);
		// 12 against 8.
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(roar));
		scn.ShadowChooseOptionalTrigger(roar);
		scn.ShadowChooseCard(gimli);
		assertEquals(1, scn.GetCultureTokensOn(roar));
		assertEquals(1, scn.GetWoundsOn(gimli));

		// The fierce skirmish: the Captain beats Aragorn again.
		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(aragorn, captain);
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassFierceSkirmishActions();
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(roar));
		scn.ShadowChooseOptionalTrigger(roar);
		scn.ShadowChooseCard(gimli);
		assertEquals(0, scn.GetCultureTokensOn(roar));
		assertEquals(2, scn.GetWoundsOn(gimli));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WithEveryCompanionExhaustedTheTokenIsRemovedForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var roar = scn.GetShadowCard("roar");
		var feral = scn.GetShadowCard("feral");
		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var guard = scn.GetFreepsCard("guard");
		scn.MoveCompanionsToTable(aragorn, guard);
		scn.MoveCardsToSupportArea(roar);
		scn.AddTokensToCard(roar, 1);
		// Frodo and Aragorn, of vitality 4, exhausted.
		scn.AddWoundsToChar(frodo, 3);
		scn.AddWoundsToChar(aragorn, 3);
		assertEquals(1, scn.GetCultureTokensOn(roar));
		assertEquals(3, scn.GetWoundsOn(frodo));
		assertEquals(3, scn.GetWoundsOn(aragorn));

		scn.StartGame();
		scn.MoveMinionsToTable(feral);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(guard, feral);
		scn.FreepsResolveSkirmish(guard);
		// 9 against 4: the Dwarf Guard is overwhelmed.
		scn.PassSkirmishActions();

		assertInZone(Zone.DEAD, guard);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(roar));
		scn.ShadowChooseOptionalTrigger(roar);
		assertEquals(0, scn.GetCultureTokensOn(roar));
		assertEquals(3, scn.GetWoundsOn(frodo));
		assertEquals(3, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
