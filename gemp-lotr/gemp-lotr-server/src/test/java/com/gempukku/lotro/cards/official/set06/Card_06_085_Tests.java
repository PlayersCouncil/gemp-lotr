package com.gempukku.lotro.cards.official.set06;

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

public class Card_06_085_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("sword", "6_85");
					// Úlairë Toldëa, Wraith on Wings: fierce [wraith] Nazgûl, strength 12, vitality 3; his other text is
					// a discount on his own play
					put("toldea", "7_219");
					// Úlairë Lemenya, Lieutenant of Morgul: a [wraith] Nazgûl of another title, strength 9, vitality 2.
					// "Each companion or ally who bears a [wraith] condition is strength -2": nobody here bears one.
					put("lemenya", "1_232");
					// Ulaire Attea, Khamul of the East: a [raider] Nazgûl; his texts need 3 burdens
					put("khamul", "103_61");
					// Morgul Creeper: a [wraith] Orc, no Nazgûl; its text needs 3 burdens
					put("creeper", "8_75");
					// Not Easily Destroyed: a [wraith] condition in the Shadow support area; an ability used only on purpose
					put("ned", "1_220");

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8, vitality 4; a maneuver ability used only on purpose
					put("boromir", "1_97");     // Boromir, Son of Denethor: strength 7, vitality 3; a skirmish ability used only on purpose
					put("gimli", "1_13");       // Gimli, Son of Gloin: a Dwarf companion; a skirmish ability used only on purpose
					put("cloak", "1_42");       // Elven Cloak: an [elven] possession on a companion; minion archery total -1
					put("armor", "1_8");        // Dwarven Armor: a [dwarven] possession on a Dwarf; changes no number
					// The Saga of Elendil: a [gondor] condition on a [gondor] companion, vitality +1; an ability used only on purpose
					put("saga", "1_114");
					// Mallorn-trees: an [elven] condition in the support area; a fellowship ability only
					put("mallorn", "1_54");
					// The Shards of Narsil: a [gondor] artifact in the support area; fellowship abilities only
					put("shards", "3_44");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void SwordofDolGuldurStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 6
		 * Name: Sword of Dol Guldur
		 * Unique: True
		 * Side: Shadow
		 * Culture: Wraith
		 * Twilight Cost: 2
		 * Type: Possession
		 * Subtype: Hand Weapon
		 * Strength: +2
		 * Game Text: Bearer must be a Nazgûl.
		 * 	While bearer is Úlairë Toldëa, each time he wins a skirmish the Free Peoples player must discard a Free Peoples possession or a Free Peoples condition.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("sword");

		assertEquals("Sword of Dol Guldur", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.WRAITH, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertTrue(card.getBlueprint().getPossessionClasses().contains(PossessionClass.HAND_WEAPON));
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(2, card.getBlueprint().getStrength());
	}

	@Test
	public void PlaysOnANazgulOfEitherCultureAndNotOnAWraithOrc() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var sword = scn.GetShadowCard("sword");
		var toldea = scn.GetShadowCard("toldea");
		var khamul = scn.GetShadowCard("khamul");
		var creeper = scn.GetShadowCard("creeper");

		scn.StartGame();
		scn.MoveCardsToHand(sword);
		assertInHand(sword);
		scn.SetTwilight(5);
		scn.FreepsPass();
		scn.MoveMinionsToTable(toldea, khamul, creeper);
		scn.ShadowRefreshDecision();

		assertTrue(scn.ShadowPlayAvailable(sword));
		scn.ShadowPlayCard(sword);

		// Two Nazgûl of two cultures are offered; a [wraith] Orc who is no Nazgûl, and the companion, are not.
		assertTrue(scn.ShadowHasCardChoiceAvailable(toldea, khamul));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(creeper, aragorn));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(khamul);

		assertAttachedTo(sword, khamul);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void EachTimeToldeaWinsBearingItTheFreePeoplesPlayerDiscardsAFreePeoplesPossessionOrConditionAndNotWhenAnotherNazgulWins() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		var gimli = scn.GetFreepsCard("gimli");
		var cloak = scn.GetFreepsCard("cloak");
		var armor = scn.GetFreepsCard("armor");
		var saga = scn.GetFreepsCard("saga");
		var mallorn = scn.GetFreepsCard("mallorn");
		var shards = scn.GetFreepsCard("shards");
		scn.MoveCompanionsToTable(aragorn, boromir, gimli);
		scn.AttachCardsTo(frodo, cloak);
		scn.AttachCardsTo(gimli, armor);
		scn.AttachCardsTo(aragorn, saga);
		scn.MoveCardsToSupportArea(mallorn, shards);

		var sword = scn.GetShadowCard("sword");
		var toldea = scn.GetShadowCard("toldea");
		var lemenya = scn.GetShadowCard("lemenya");
		var ned = scn.GetShadowCard("ned");

		scn.StartGame();
		scn.MoveMinionsToTable(toldea, lemenya);
		scn.AttachCardsTo(toldea, sword);
		scn.MoveCardsToSupportArea(ned);
		assertAttachedTo(cloak, frodo);
		assertAttachedTo(armor, gimli);
		assertAttachedTo(saga, aragorn);
		assertInZone(Zone.SUPPORT, mallorn, shards, ned);
		assertAttachedTo(sword, toldea);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[] { aragorn, toldea }, new PhysicalCardImpl[] { boromir, lemenya });

		// 9 against 7: Lemenya, another Nazgûl who does not bear the Sword, wins.  Boromir takes his wound and
		// nothing else happens.
		assertEquals(0, scn.GetWoundsOn(boromir));
		scn.FreepsResolveSkirmish(boromir);
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(boromir));
		assertTrue(scn.FreepsAwaitingSkirmishChoice());

		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();

		// 12 + 2 against 8: he wins, without overwhelming Aragorn.
		assertAttachedTo(cloak, frodo);
		assertAttachedTo(armor, gimli);
		assertAttachedTo(saga, aragorn);
		assertInZone(Zone.SUPPORT, mallorn, shards, ned);
		assertAttachedTo(sword, toldea);
		scn.FreepsResolveRuleFirst();

		// The Free Peoples player chooses: the Free Peoples possessions and conditions, borne or in the support area.
		// Not a Free Peoples artifact, not the Sword (a Shadow possession), not a Shadow condition.
		assertTrue(scn.FreepsHasCardChoiceAvailable(cloak, armor, saga, mallorn));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(shards, sword, ned));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(cloak);

		assertInZone(Zone.DISCARD, cloak);
		assertAttachedTo(armor, gimli);
		assertAttachedTo(saga, aragorn);
		assertInZone(Zone.SUPPORT, mallorn, shards, ned);
		assertAttachedTo(sword, toldea);

		// He is fierce, and wins again: the Free Peoples player discards again, a condition this time.
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(aragorn, toldea);
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();
		scn.FreepsResolveRuleFirst();

		assertTrue(scn.FreepsHasCardChoiceAvailable(armor, saga, mallorn));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(shards, sword, ned));
		scn.FreepsChooseCard(saga);

		assertInZone(Zone.DISCARD, cloak, saga);
		assertAttachedTo(armor, gimli);
		assertInZone(Zone.SUPPORT, mallorn, shards, ned);
		assertAttachedTo(sword, toldea);
		assertEquals(Phase.REGROUP, scn.GetCurrentPhase());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void ToldeaLosingBearingItDiscardsNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var cloak = scn.GetFreepsCard("cloak");
		var mallorn = scn.GetFreepsCard("mallorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.AttachCardsTo(frodo, cloak);
		scn.MoveCardsToSupportArea(mallorn);

		var sword = scn.GetShadowCard("sword");
		var toldea = scn.GetShadowCard("toldea");

		scn.StartGame();
		scn.MoveMinionsToTable(toldea);
		scn.AttachCardsTo(toldea, sword);
		// A strength that only arranges the board: 8 + 7 = 15 beats his 14.
		scn.ApplyAdHocModifier(new StrengthModifier(null, aragorn, null, 7));
		assertAttachedTo(cloak, frodo);
		assertInZone(Zone.SUPPORT, mallorn);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, toldea);
		scn.FreepsResolveSkirmish(aragorn);
		assertEquals(0, scn.GetWoundsOn(toldea));
		scn.PassSkirmishActions();

		assertEquals(1, scn.GetWoundsOn(toldea));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertAttachedTo(cloak, frodo);
		assertInZone(Zone.SUPPORT, mallorn);
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void WithAnotherNazgulBearingItNeitherHisWinNorToldeasDiscardsAnything() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		var cloak = scn.GetFreepsCard("cloak");
		var mallorn = scn.GetFreepsCard("mallorn");
		scn.MoveCompanionsToTable(aragorn, boromir);
		scn.AttachCardsTo(frodo, cloak);
		scn.MoveCardsToSupportArea(mallorn);

		var sword = scn.GetShadowCard("sword");
		var toldea = scn.GetShadowCard("toldea");
		var lemenya = scn.GetShadowCard("lemenya");

		scn.StartGame();
		scn.MoveMinionsToTable(toldea, lemenya);
		scn.AttachCardsTo(lemenya, sword);
		assertAttachedTo(sword, lemenya);
		assertAttachedTo(cloak, frodo);
		assertInZone(Zone.SUPPORT, mallorn);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[] { aragorn, lemenya }, new PhysicalCardImpl[] { boromir, toldea });

		// 9 + 2 against 8: Lemenya, bearing the Sword, wins.  Aragorn takes his wound and nothing else happens.
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(boromir));
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertAttachedTo(cloak, frodo);
		assertInZone(Zone.SUPPORT, mallorn);

		// 12 against 7: Toldëa wins, not bearing it.  Boromir takes his wound and nothing else happens.
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
		scn.FreepsResolveSkirmish(boromir);
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(boromir));
		assertAttachedTo(cloak, frodo);
		assertInZone(Zone.SUPPORT, mallorn);
		assertAttachedTo(sword, lemenya);
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}
}
