package com.gempukku.lotro.cards.official.set07;

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

public class Card_07_241_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("armor", "7_241");
					put("merry", "7_320");      // Merry, Rohirrim Squire: [shire] Hobbit, strength 3, vitality 4; a skirmish ability used only on purpose
					put("pippin", "1_306");     // Pippin, Friend to Frodo: a Hobbit companion who is not Merry; text about [shire] tales only
					put("guard1", "5_83");      // Household Guard: [rohan] Man companion, strength 6, vitality 3, Valiant and nothing else
					put("guard2", "5_83");
					put("guma", "4_277");       // Guma, Plains Farmer: [rohan] Man ally; a fellowship ability used only on purpose
					put("knight", "5_35");      // Gondorian Knight: a [gondor] Man companion, Knight and nothing else
					put("troop1", "1_177");     // Goblin Patrol Troop: [moria] Orc minion, strength 13, no text
					put("troop2", "1_177");
					put("troop3", "1_177");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetRohanMerryScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("armor", "7_241");
					put("merry", "103_85");     // Merry, Master Holbytla: the [rohan] Merry, a Hobbit companion
					put("pippin", "1_306");     // Pippin, Friend to Frodo: a Hobbit companion who is not Merry
					put("guard", "5_83");       // Household Guard: [rohan] Man companion, Valiant and nothing else
					put("guma", "4_277");       // Guma, Plains Farmer: [rohan] Man ally; a fellowship ability used only on purpose
					put("leowyn", "7_239");     // Leowyn: [rohan] Man ally; a fellowship ability used only on purpose
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc minion, strength 13, no text
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void MerrysArmorStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Merry's Armor
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Rohan
		 * Twilight Cost: 0
		 * Type: Possession
		 * Subtype: Armor
		 * Strength: +2
		 * Vitality: -1
		 * Game Text: Bearer must be Merry.
		 * While you can spot 3 [rohan] Men, Merry cannot be overwhelmed unless his strength is tripled.
		*/

		var scn = GetScenario();

		var armor = scn.GetFreepsCard("armor");

		assertEquals("Merry's Armor", armor.getBlueprint().getTitle());
		assertNull(armor.getBlueprint().getSubtitle());
		assertTrue(armor.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, armor.getBlueprint().getSide());
		assertEquals(Culture.ROHAN, armor.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, armor.getBlueprint().getCardType());
		assertTrue(armor.getBlueprint().getPossessionClasses().contains(PossessionClass.ARMOR));
		assertEquals(1, armor.getBlueprint().getPossessionClasses().size());
		assertEquals(0, armor.getBlueprint().getTwilightCost());
		assertEquals(2, armor.getBlueprint().getStrength());
		assertEquals(-1, armor.getBlueprint().getVitality());
	}

	@Test
	public void PlaysOnMerryOnly() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var armor = scn.GetFreepsCard("armor");
		var merry = scn.GetFreepsCard("merry");
		var pippin = scn.GetFreepsCard("pippin");
		var guard1 = scn.GetFreepsCard("guard1");
		scn.MoveCompanionsToTable(merry, pippin, guard1);
		scn.MoveCardsToHand(armor);
		assertInHand(armor);

		scn.StartGame();

		assertTrue(scn.FreepsPlayAvailable(armor));
		scn.FreepsPlayCard(armor);

		// Merry is the one bearer it can go on: the other Hobbit and the [rohan] Man are not asked about.
		assertAttachedTo(armor, merry);
		assertNotAttachedTo(armor, pippin);
		assertNotAttachedTo(armor, guard1);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void CannotBePlayedWithoutMerryAndTheRohanMerryMayBearIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetRohanMerryScenario();

		var armor = scn.GetFreepsCard("armor");
		var merry = scn.GetFreepsCard("merry");
		var pippin = scn.GetFreepsCard("pippin");
		var guard = scn.GetFreepsCard("guard");
		scn.MoveCompanionsToTable(pippin, guard);
		scn.MoveCardsToHand(armor, merry);
		assertInHand(armor, merry);

		scn.StartGame();

		// A Hobbit companion, a [rohan] Man and the Ring-bearer, and no Merry.
		assertFalse(scn.FreepsPlayAvailable(armor));

		scn.FreepsPlayCard(merry);
		// This Merry is [rohan], not [shire]; the text names only the title.
		assertEquals(Culture.ROHAN, merry.getBlueprint().getCulture());
		assertTrue(scn.FreepsPlayAvailable(armor));
		scn.FreepsPlayCard(armor);

		assertAttachedTo(armor, merry);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void OnceAThirdRohanManIsPlayedMerryIsNotOverwhelmedAtDoubleStrengthAndAGuardIs() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var armor = scn.GetFreepsCard("armor");
		var merry = scn.GetFreepsCard("merry");
		var guard1 = scn.GetFreepsCard("guard1");
		var guard2 = scn.GetFreepsCard("guard2");
		var guma = scn.GetFreepsCard("guma");
		var pippin = scn.GetFreepsCard("pippin");
		var troop1 = scn.GetShadowCard("troop1");
		var troop2 = scn.GetShadowCard("troop2");
		var troop3 = scn.GetShadowCard("troop3");
		scn.MoveCompanionsToTable(merry, guard1, pippin);
		scn.MoveAlliesToTable(guma);
		scn.AttachCardsTo(merry, armor);
		scn.MoveCardsToHand(guard2);
		assertAttachedTo(armor, merry);
		assertInHand(guard2);

		scn.StartGame();

		// The Guard and Guma are two [rohan] Men; the second Guard, played, is the third.
		scn.FreepsPlayCard(guard2);
		assertInZone(Zone.FREE_CHARACTERS, guard2);

		scn.MoveMinionsToTable(troop1, troop2, troop3);
		// 13 + 1, a number that only sets the board: at least double Merry's 5, one short of triple.
		scn.ApplyAdHocModifier(new StrengthModifier(null, troop1, null, 1));
		// Pippin 3 + 2: more than double against him, less than triple, as the Guard's 6 is.
		scn.ApplyAdHocModifier(new StrengthModifier(null, pippin, null, 2));
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{merry, troop1}, new PhysicalCardImpl[]{guard1, troop2}, new PhysicalCardImpl[]{pippin, troop3});

		scn.FreepsResolveSkirmish(merry);
		// Merry 3 + 2 for the Armor.
		assertEquals(5, scn.GetStrength(merry));
		assertEquals(14, scn.GetStrength(troop1));
		assertEquals(0, scn.GetWoundsOn(merry));
		scn.PassSkirmishActions();
		// Spared the overwhelm, he takes the 1 wound for losing.
		assertInPlay(merry);
		assertEquals(1, scn.GetWoundsOn(merry));

		// Another [shire] Hobbit companion who is not Merry, at more than double against him: overwhelmed.
		scn.FreepsResolveSkirmish(pippin);
		assertEquals(5, scn.GetStrength(pippin));
		assertEquals(13, scn.GetStrength(troop3));
		scn.PassSkirmishActions();
		assertInZone(Zone.DEAD, pippin);
		assertEquals(1, scn.GetFreepsDeadCount());

		// A [rohan] Man who bears no Armor, at more than double against him (13 against 6): overwhelmed.
		scn.FreepsResolveSkirmish(guard1);
		assertEquals(6, scn.GetStrength(guard1));
		assertEquals(13, scn.GetStrength(troop2));
		assertEquals(1, scn.GetFreepsDeadCount());
		scn.PassSkirmishActions();
		assertInZone(Zone.DEAD, guard1);
		assertEquals(2, scn.GetFreepsDeadCount());

		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WithOnlyTwoRohanMenMerryIsOverwhelmedAtDoubleStrength() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var armor = scn.GetFreepsCard("armor");
		var merry = scn.GetFreepsCard("merry");
		var guard1 = scn.GetFreepsCard("guard1");
		var guma = scn.GetFreepsCard("guma");
		var knight = scn.GetFreepsCard("knight");
		var troop1 = scn.GetShadowCard("troop1");
		// Two [rohan] Men (the Guard and Guma), a [gondor] Man and the Armor, a [rohan] card that is no Man.
		scn.MoveCompanionsToTable(merry, guard1, knight);
		scn.MoveAlliesToTable(guma);
		scn.AttachCardsTo(merry, armor);
		assertAttachedTo(armor, merry);

		scn.StartGame();
		scn.MoveMinionsToTable(troop1);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(merry, troop1);
		scn.FreepsResolveSkirmish(merry);

		assertEquals(5, scn.GetStrength(merry));
		assertEquals(13, scn.GetStrength(troop1));
		assertEquals(0, scn.GetWoundsOn(merry));
		assertEquals(0, scn.GetFreepsDeadCount());
		scn.PassSkirmishActions();

		// Merry has vitality 3 (4, -1 for the Armor): one wound would not kill him; the overwhelm does.
		assertInZone(Zone.DEAD, merry);
		assertEquals(1, scn.GetFreepsDeadCount());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WithThreeRohanMenMerryIsOverwhelmedOnceHisStrengthIsTripled() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var armor = scn.GetFreepsCard("armor");
		var merry = scn.GetFreepsCard("merry");
		var guard1 = scn.GetFreepsCard("guard1");
		var guard2 = scn.GetFreepsCard("guard2");
		var guma = scn.GetFreepsCard("guma");
		var troop1 = scn.GetShadowCard("troop1");
		scn.MoveCompanionsToTable(merry, guard1, guard2);
		scn.MoveAlliesToTable(guma);
		scn.AttachCardsTo(merry, armor);
		assertAttachedTo(armor, merry);

		scn.StartGame();
		scn.MoveMinionsToTable(troop1);
		// 13 + 2, a number that only sets the board: exactly triple Merry's 5.
		scn.ApplyAdHocModifier(new StrengthModifier(null, troop1, null, 2));
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(merry, troop1);
		scn.FreepsResolveSkirmish(merry);

		assertEquals(5, scn.GetStrength(merry));
		assertEquals(15, scn.GetStrength(troop1));
		assertEquals(0, scn.GetWoundsOn(merry));
		assertEquals(0, scn.GetFreepsDeadCount());
		scn.PassSkirmishActions();

		assertInZone(Zone.DEAD, merry);
		assertEquals(1, scn.GetFreepsDeadCount());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void TheRohanMerryBearingItIsNotOverwhelmedAtDoubleStrengthEither() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetRohanMerryScenario();

		var armor = scn.GetFreepsCard("armor");
		var merry = scn.GetFreepsCard("merry");
		var guard = scn.GetFreepsCard("guard");
		var guma = scn.GetFreepsCard("guma");
		var leowyn = scn.GetFreepsCard("leowyn");
		var troop = scn.GetShadowCard("troop");
		// Three [rohan] Men: the Guard, Guma and Leowyn.  Merry and the Guard are only two [rohan] companions, so
		// this Merry is a Hobbit and no Man himself.
		scn.MoveCompanionsToTable(merry, guard);
		scn.MoveAlliesToTable(guma, leowyn);
		scn.AttachCardsTo(merry, armor);
		assertAttachedTo(armor, merry);

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		// 13 + 1, a number that only sets the board: at least double Merry's 5, one short of triple.
		scn.ApplyAdHocModifier(new StrengthModifier(null, troop, null, 1));
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(merry, troop);
		scn.FreepsResolveSkirmish(merry);

		// Merry 3 + 2 for the Armor; unwounded, so Enduring adds nothing.
		assertEquals(5, scn.GetStrength(merry));
		assertEquals(14, scn.GetStrength(troop));
		assertEquals(0, scn.GetWoundsOn(merry));
		scn.PassSkirmishActions();

		assertInPlay(merry);
		assertEquals(1, scn.GetWoundsOn(merry));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
